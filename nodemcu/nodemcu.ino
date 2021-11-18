
#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <Servo.h>

// firebase url
#define FIREBASE_HOST "smart-food-container-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "p3AP0oiig5bpQbNl4x38Cr00um3anVAi22TVUL2w"
#define WIFI_SSID "FiberHGW_ZTQR32_2.4GHz"
#define WIFI_PASSWORD "HR9uueztCE"

// define pin on nodemcu
#define trigP D1
#define echoP D2
#define buzzer D3

long duration;
int distance;
int servo_angle = 0;
Servo servo;

void setup() {
  pinMode(trigP, OUTPUT);
  pinMode(echoP, INPUT);
  pinMode(buzzer,OUTPUT);
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

  // connect to firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}

String state = "false";

void loop() {
  /*
    digitalWrite(trigP, LOW);   // Makes trigPin low
    delayMicroseconds(2);       // 2 micro second delay

    digitalWrite(trigP, HIGH);  // tigPin high
    delayMicroseconds(10);      // trigPin high for 10 micro seconds
    digitalWrite(trigP, LOW);   // trigPin low

    duration = pulseIn(echoP, HIGH);   //Read echo pin, time in microseconds
    distance = duration * 0.034 / 2;   //Calculating actual/real distance

    Serial.print("Distance = ");        //Output distance on arduino serial monitor
    Serial.println(distance);
    Firebase.setInt("distance",distance);
  */


  state = Firebase.getString("State");
  if (state == "True") {
    digitalWrite(buzzer,HIGH);
    servo.write(180);
    delay(1000);
    servo.write(0);
    Firebase.setString("State", "False");
    digitalWrite(buzzer,LOW);
  }


}
