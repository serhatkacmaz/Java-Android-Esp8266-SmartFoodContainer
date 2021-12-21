
#include <FirebaseESP8266.h>
#include <ESP8266WiFi.h>
#include <Servo.h>
#include <NTPClient.h>
#include <WiFiUdp.h>


// firebase url
#define FIREBASE_HOST ""
#define FIREBASE_AUTH ""
#define WIFI_SSID ""
#define WIFI_PASSWORD ""

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

int currentHour, currentMinute, currentSecond;

FirebaseData Data;
Servo servo;

void setup() {
  // connect to wifi
  Serial.begin(9600);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());
  
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

  pinMode(trigP, OUTPUT);
  pinMode(echoP, INPUT);
  pinMode(buzzer, OUTPUT);
  servo.attach(D4);
  servo.write(55);
  delay(500);
  Serial.begin(9600);
  // Initialize a NTPClient to get time
  // https://randomnerdtutorials.com/esp8266-nodemcu-date-time-ntp-client-server-arduino/
  timeClient.begin();
  timeClient.setTimeOffset(10800); // GMT +3
  // https://randomnerdtutorials.com/esp8266-nodemcu-date-time-ntp-client-server-arduino/
}

String now_time = "";
int alarm_count = 0;
int open_close_count;
String state = "false";
int distance_bowl = 11;
void loop() {

  nowTime();
  formatAlarmClock();
  hc04();

  Firebase.getInt(Data,"/OpenCloseCount",open_close_count);
    
  // 1 tolerans, Mama verme isteği olursa mama ver
  if (distance == distance_bowl || distance == distance_bowl - 1 || distance == distance_bowl + 1) {
    Firebase.setString(Data,"/bowl_state", "Empty");
    Firebase.getString(Data,"/State",state);
    // Hazne bos degil
    if (state == "True") {
      giveFood();
      Firebase.setString(Data,"/State", "False");
    }
  }
  else {
    Firebase.setString(Data,"/bowl_state", "Full");
  }


  // gelen alarm var ise mama ver
  Firebase.getInt(Data,"/AlarmCount",alarm_count);
  for (int i = 1; i <= alarm_count; i++) {
    String get_alarm;
    Firebase.getString(Data,"/Alarm/" + String(i),get_alarm);
    
    if (now_time == get_alarm && open_close_count < 4 &&(distance == distance_bowl || distance == distance_bowl - 1 || distance == distance_bowl + 1)) {
      Serial.println("Alarmmm Calıyor");
      giveFood();

      // açılma kapanmayı 1 arttır
      Firebase.setInt(Data,"/OpenCloseCount", open_close_count + 1);
      Firebase.setString(Data,"/bowl_state","Full");
      delay(60000);  // 1 dk bekle
      break;
    }
  }
}

// mama ver
void giveFood() {
  digitalWrite(buzzer, HIGH);
  servo.write(0);
  delay(1000);
  servo.write(55);
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
