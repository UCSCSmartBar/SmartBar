import sys
import analogspi as SPI
thresh_hi = 400
thresh_lo = 200
cupChannel = 3

def CupDetect():
    if not hasattr(CupDetect,'lastval'):
        CupDetect.lastval = 0
    if (SPI.ReadChannel(cupChannel)<thresh_lo):
        if CupDetect.lastval == 0:
            print 'cupdetect: cup placed'
            CupDetect.lastval = 1
    elif (SPI.ReadChannel(cupChannel)>thresh_hi):
        if (CupDetect.lastval == 1):
            print 'cupdetect: cupremoved'
            CupDetect.lastval = 0
    return CupDetect.lastval

##SPI.InitSPI()
##while (True):
##    CupDetect()
