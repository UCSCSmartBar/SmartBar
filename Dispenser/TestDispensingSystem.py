import DispensingSystem as SBDispenser

def main():

                                            
    Dispenser = SBDispenser.SmartBar_Dispenser()



    Dispenser.ReceiveCommand("$DO,1,1@WH,1,0.1@GT,1,0,0.1")
 #   Dispenser.GPIO_Free()
    
if __name__ == '__main__':
    main()
