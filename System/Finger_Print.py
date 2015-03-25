import FPS, sys

IDLE = 0
GET_FIRST = 1
GET_SECOND = 2
GET_THIRD = 3
STORE_FINGER = 4
WARNING = 5

def Finger_PrintSM(msg):
    fps =  FPS.FPS_GT511C3(device_name='/dev/ttyAMA0',baud=9600,timeout=2,is_com=False)
    fps.UseSerialDebug = True
    fps.SetLED(True) # Turns ON the CMOS LED
    FPS.delay(1) # wait 1 second
    print 'Put your finger in the scan'
    
    if not hasattr(Finger_PrintSM,"state"):
        #Declare intial state
        Finger_PrintSM.state = IDLE
        print Finger_PrintSM.state

    print 'CurState:[' + Finger_PrintSM+']'
    print 'Cmd:[' + msg + ']'

    if(Finger_PrintSM.state == IDLE):
       if(msg == 'First'):
          Finger_PrintSM.state = GET_FIRST

    elif(Finger_PrintSM.state == GET_FIRST):
        if(msg == 'Second'):
            Finger_PrintSM.state = GET_SECOND
          
    elif(Finger_PrintSM.state is GET_SECOND):
        if(msg == 'Third'):
            Finger_PrintSM.state = GET_THIRD
        
    elif(Finger_PrintSM.state is GET_THIRD):
        if(msg == 'Finish'):
            Finger_PrintSM.state = STORE_FINGER
        
    elif(Finger_PrintSM.state is STORE_FINGER):
        print 'About to store finger'
        Finger_PrintSM.state = IDLE
    elif(Finger_PrintSM.state is WARNING):
        print 'Warning'
    else:
        print 'unknown state'
    
#atribute to be initialized 

def enroll
    ID=0; count=0
    unavail=True
    
    

