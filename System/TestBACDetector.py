import BACDetector
import time as t

class BACSystemTest():
    def __init__(self):
        BACSystemTest.BACDetect = BACDetector.SmartBar_BACDetector()    

    def TestGetBACValue(self):
        self.BACDetect.CollectBACSample()

    def ReadBACValue(self):
        self.BACDetect.ReadBACSensor()

            
def main():

                                            
    Tester = BACSystemTest()

    SampleNumber = 0
    Tester.TestGetBACValue()
    EndTime = t.time() + 1
    CurrentTime = t.time()
    while(CurrentTime < EndTime):
        CurrentTime = t.time()
        pass
        
    Tester.TestGetBACValue()


    #while(1 != 0):
  #      if (CurrentTime > EndTime):
  #          Tester.TestGetBACValue()
  #          EndTime = t.time() + 5
  #      else:
  #          CurrentTime = t.time()
  #  print('ByeBye')

    
if __name__ == '__main__':
    main()


