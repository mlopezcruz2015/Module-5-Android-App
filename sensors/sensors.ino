#include <LiquidCrystal_I2C.h>
#include <Wire.h>
#include <Servo.h>

#include <SoftwareSerial.h> // use the software uart
SoftwareSerial bluetooth(9, 10); // RX, TX
LiquidCrystal_I2C lcd = LiquidCrystal_I2C(0x27, 16, 2);
Servo servo;

void setup() 
{
  Serial.begin(9600);
  bluetooth.begin(9600);
  delay(200);
  lcd.init();
  lcd.backlight();

  pinMode(3, OUTPUT);
  pinMode(2, INPUT);
}


String btString = "";
float temperature;
long duration;
int distance;

void loop() 
{
  digitalWrite(3, LOW);
  delay(2);
  digitalWrite(3, HIGH);
  delayMicroseconds(10);
  digitalWrite(3, LOW);
  duration = pulseIn(2, HIGH);
  distance = duration*0.034/2; //CENTIMETERS
  Serial.println(distance);
  
  temperature = analogRead(A0)*5/1024.0;
  temperature = temperature - 0.5;
  temperature = temperature / 0.01;
  
  lcd.setCursor(0, 1);
  lcd.print(temperature);
  lcd.print(" C. ");

  lcd.setCursor(10, 1);
  lcd.print(distance);
  lcd.print("  ");
  lcd.setCursor(14, 1);
  lcd.print("cm");
  
  if (bluetooth.available())
  {
    while(bluetooth.available()>0)
    {
      char character = bluetooth.read();
      btString = btString + character;
    }
  }

  if (btString != "")
  {
    Serial.println(btString);
    if (btString == "get temp")
    {
      bluetooth.print(temperature);
    }
    else if (btString == "get distance")
    {
      bluetooth.print(distance);
    }
    else
    {      
      lcd.clear();
      btString.replace("get temp","");
      btString.replace("get distance","");
      lcd.setCursor(0, 0); // Set the cursor on the first column and first row.
      lcd.print(btString); // Print the string "Hello World!"
    }

    btString = "";
  }

  
    delay(500);
}
