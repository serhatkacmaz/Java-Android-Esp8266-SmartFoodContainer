
#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <Servo.h>

// firebase url
#define FIREBASE_HOST "smart-food-container-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "p3AP0oiig5bpQbNl4x38Cr00um3anVAi22TVUL2w"
#define WIFI_SSID "FiberHGW_ZTQR32_2.4GHz"
#define WIFI_PASSWORD "HR9uueztCE"

// Define NTP Client to get time
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

// define pin on nodemcu
#define trigP D1
#define echoP D2
#define buzzer D3

// variables
long duration;
int distance;

int servo_angle = 0;
int currentHour, currentMinute, currentSecond;
Servo servo;
String state = "false";
int distance_bowl = 8;

void setup() {
  pinMode(trigP, OUTPUT);
  pinMode(echoP, INPUT);
  pinMode(buzzer, OUTPUT);
  servo.attach(D4);
  servo.write(servo_angle);
  delay(500);
  Serial.begin(9600);

  // connect to wifi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  // Initialize a NTPClient to get time
  // https://randomnerdtutorials.com/esp8266-nodemcu-date-time-ntp-client-server-arduino/
  timeClient.begin();
  timeClient.setTimeOffset(10800); // GMT +3
  // https://randomnerdtutorials.com/esp8266-nodemcu-date-time-ntp-client-server-arduino/

  // connect to firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}

String now_time = "";
int alarm_count = 0;
int open_close_count = 0;

void loop() {

  nowTime();
  formatAlarmClock();
  hc04();

  open_close_count = Firebase.getInt("OpenCloseCount");

  // 1 tolerans, Mama verme isteği olursa mama ver
  if (distance == distance_bowl || distance == distance_bowl - 1 || distance == distance_bowl + 1) {
    Firebase.setString("bowl_state", "Empty");
    state = Firebase.getString("State");
    // Hazne bos degil
    if (state == "True") {
      giveFood();
      Firebase.setString("State", "False");
    }
  }
  else {
    Firebase.setString("bowl_state", "Full");
  }


  // gelen alarm var ise mama ver
  alarm_count = Firebase.getInt("AlarmCount");
  for (int i = 1; i <= alarm_count; i++) {
    String get_alarm = Firebase.getString("Alarm/" + String(i));

    if (now_time == get_alarm && open_close_count < 4 &&(distance == distance_bowl || distance == distance_bowl - 1 || distance == distance_bowl + 1)) {
      Serial.println("Alarmmm Calıyor");
      giveFood();

      // açılma kapanmayı 1 arttır
      Firebase.setInt("OpenCloseCount", open_close_count + 1);
      Firebase.setString("bowl_state","Full");
      delay(60000);  // 1 dk bekle
      break;
    }
  }
}

// mama ver
void giveFood() {
  digitalWrite(buzzer, LOW);
  servo.write(180);
  delay(1000);
  servo.write(0);
  digitalWrite(buzzer, LOW);
}


// sunucudan gelen saati formatlama
void formatAlarmClock() {
  if (now_time[1] == ':' && now_time.length() == 3) {
    // 1:1  --> 01:01
    String temp = "0";
    temp += now_time[0];
    temp += ":0";
    temp += now_time[2];
    now_time = temp;
  }
  else if (now_time[1] == ':' && now_time.length() == 4) {
    // 1:25 --> 01:25
    String temp = "0";
    temp += now_time;
    now_time = temp;
  }
  else if (now_time[2] == ':' && now_time.length() == 4) {
    // 12:7 --> 12:07
    String temp = "";
    temp += now_time[0];
    temp += now_time[1];
    temp += ":0";
    temp += now_time[3];
    now_time = temp;
  }
}

// aktif saat bilgisi cekme
void nowTime() {
  timeClient.update();

  currentHour = timeClient.getHours();
  currentMinute = timeClient.getMinutes();
  now_time = (String)currentHour + ":" + (String)currentMinute;
}

// mesafe sensoru ile mesafe tespiti
void hc04() {
  Serial.print(distance_bowl);
  digitalWrite(trigP, LOW);   // Makes trigPin low
  delayMicroseconds(2);       // 2 micro second delay

  digitalWrite(trigP, HIGH);  // tigPin high
  delayMicroseconds(10);      // trigPin high for 10 micro seconds
  digitalWrite(trigP, LOW);   // trigPin low

  duration = pulseIn(echoP, HIGH);   //Read echo pin, time in microseconds
  distance = duration * 0.034 / 2;   //Calculating actual/real distance

  Serial.print(" Distance = ");        //Output distance on arduino serial monitor
  Serial.println(distance);
}
