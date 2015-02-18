/*
    Author: Molly Graham
 Start Date: 2/6/2015
 Purpose: To test the functionality of the fingerprint sensor    
 */

#include "FPS_GT511C3.h"
#include "SoftwareSerial.h"

FPS_GT511C3 fps(4, 5);

void setup()
{
  Serial.begin(9600);
  delay(100);
  fps.Open();
  fps.SetLED(true);
  //enroll();
  identify();

//  if(fps.DeleteID(1)){
//    Serial.print("lol");
//  }else{
//    Serial.print("daw");
//  }

  //fps.DeleteAll();
  
  //checking if enrolled exists. tests indicates will work around indices 
  //(e.g. if 14 are enrolled, 13 will return true but 14 will return false)
  //useful for debugging other functions such as deleteid and deleteall
//  if(fps.CheckEnrolled(12)==true){
//    Serial.print("\nhey");
//  }
//  else{
//    Serial.print("\nhi");
//  }
}

void enroll(){
  //requires three snapshots of the fingerprints to enroll
  int ID=0,count=0;
  bool unavail=true;

  //check for next available ID
  while(unavail==true){
    unavail=fps.CheckEnrolled(ID);
    if(unavail==true){
      ID++;
    }
  }

  //start enrolling process to the next available ID
  fps.EnrollStart(ID);

  Serial.print("\nEnrollment takes three scans.\n Place finger on sensor, please, User #");
  Serial.println(ID+1);

  //wait for the finger the be pressed on the surface
  while(fps.IsPressFinger()==false){
    delay(100);
  }
  //capture the finger, the param is true for high quality and false for low
  bool capt=fps.CaptureFinger(false);
  int captErr=0;

  //phases of capturing the fingerprint
  if(capt==true){
    //first finger captured
    Serial.println("Please remove finger");
    fps.Enroll1();
    while(fps.IsPressFinger()==true){
      delay(100);
    }
    Serial.println(" Place the same finger a second time");
    while(fps.IsPressFinger()==false){
      delay(100);
    }
    capt=fps.CaptureFinger(false);

    //second finger captured
    if(capt==true){
      Serial.println("Please remove finger");
      fps.Enroll2();
      while(fps.IsPressFinger()==true){
        delay(100);
      }
      Serial.println(" Place the same finger a third time");
      while(fps.IsPressFinger()==false){
        delay(100);
      }
      capt=fps.CaptureFinger(false);

      //third finger captured
      if(capt==true){
        Serial.println("Please remove finger");
        captErr=fps.Enroll3();

        //checks if the enrollment failed, a bad image was taken or if the finger has already been scanned
        if(captErr==0){
          Serial.println("\nYou have been successfully enrolled");
        }
        else{
          Serial.println("\nEnrollment unsuccessful. Error code:");
          Serial.println(captErr);
          enroll();
        }
      }
      else{
        Serial.println("\nEnrollment unsuccessful, did not capture third finger.");
      }
    }
    else{
      Serial.println("\nEnrollment unsuccessful, did not capture second finger."); 
    }  
  }
  else{
    Serial.println("\nEnrollment unsuccessful, did not capture first finger."); 
  }

  char buffer[35];
  sprintf(buffer,"\nCurrent number of enrolled users:%d",fps.GetEnrollCount());
  Serial.println(buffer);
}

void identify(){
  Serial.println("\nPlace finger on sensor for identification, please.");

  //wait for finger to be placed on sensor
  while(fps.IsPressFinger()==false){
    delay(100);
  }

  //do 1:N comparison
  fps.CaptureFinger(false);
  int ID=fps.Identify1_N();;//=fps.Verify1_1(0)

  //if the user exists, welcome them. or else they aren't in database
  if(ID<200){
    Serial.println("Welcome, User #");
    Serial.print(ID+1);
  }
  else{
    Serial.println("No user found, please enroll.");
    enroll();
  }
}

void loop()
{
  //test#1: blink internal LED
  /*fps.SetLED(true);
   delay(500);
   fps.SetLED(false);
   delay(500);*/

  //  char buffer[35];
  //  sprintf(buffer,"\nCurrent number of enrolled users:%d",fps.GetEnrollCount());
  //  Serial.println(buffer);

  delay(60000);
}



