#!/usr/bin/
# author: michael sierra
import RPi.GPIO as GPIO
import analogspi as spi
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
def CIO_Free():
    print 'goodbye -love gpio'
    GPIO.cleanup()
    
def Parse_Message(string):
    if not hasattr(Parse_Message, "led0Val"):
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
    else:
        print string
        sList = string.split(".")
        print sList
        if(sList[0] == "$Valve"):
            try:
                print sList[1]
                T = int(sList[1])
                print 'T is ' + sList[1]
            except:
                print 'undef'
                return
            print 'back on'
            GPIO.output(20, 1)
            GPIO.output(26, 1)
            #Turns on LED for variable amount of time
            time.sleep(T)
            GPIO.output(20, 0)
            GPIO.output(26, 0)
        elif (sList[0] == '$DO') :
            print sListmcp[1]
        elif (sList[0] == '$AD'):
            ADstr = '$AD.' + str(spi.ReadChannel(0))            
            return ADstr
