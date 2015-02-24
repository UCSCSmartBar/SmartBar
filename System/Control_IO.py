#!/usr/bin/
# author: michael sierra
import sys
sys.path.append('/SmartBar/Dispenser/')
sys.path.append('/SmartBar/System/')
import RPi.GPIO as GPIO
import Finger_Print as FP
import analogspi as spi
import DispensingSystem as DS
import time



# param: 
# return: 
# brief: initializes GPIO, setting appropriate pins.
def CIO_Initialize():
    print('GPIO initialized')
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(26, GPIO.OUT)
    GPIO.setup(20, GPIO.OUT)
    #OnboardLED
    GPIO.setup(16,GPIO.OUT) #green
    GPIO.setup(19,GPIO.OUT) #red
    GPIO.setup(21,GPIO.OUT) #blue
    spi.InitSPI()

# param: 
# return: 
# brief: closes GPIO   
def CIO_Free():
    print 'goodbye -love gpio'
    GPIO.cleanup()

# param: string: string to be parsed
# return: string to be sent back to UI
# brief: Parses the string and calls appropriate function.
def Parse_Message(string):
    if not hasattr(Parse_Message, "led0Val"):   #toggle values
        Parse_Message.led0Val = 0
    if not hasattr(Parse_Message, "led1Val"):
        Parse_Message.led1Val = 0
    if not hasattr(Parse_Message, "LED_Val"):
        Parse_Message.LED_Val = 0
    if string == '$LED.0':
        GPIO.output(26, Parse_Message.led0Val)
        Parse_Message.led0Val = not Parse_Message.led0Val
    elif string == '$LED.1':
        GPIO.output(20, Parse_Message.led1Val)
        Parse_Message.led1Val = not Parse_Message.led1Val
    elif string == '$LED':
        Parse_Message.LED_Val = not Parse_Message.LED_Val
    elif string == '$Error':
        return 'Error'
    elif string == '$Warn':
        return 'Warning'
    else:                                       #strings to parse
        print string
        sList = string.split(".")
        print sList
        if (sList[0] == '$DO') :
            Dispenser = DS.SmartBar_Dispenser()
            Dispenser.ReceiveCommand(string)
            print('hi mom')
            del Dispenser
        elif (sList[0] == '$AD'):               #returns ADC value
            ADstr = '$AD.' + str(spi.ReadChannel(sList[1]))            
            return ADstr
        elif (sList[0] == '$FP'):
            print("Acknowledged Finger Print Command")
            FP.Finger_PrintSM(sList[1])
            return

        
