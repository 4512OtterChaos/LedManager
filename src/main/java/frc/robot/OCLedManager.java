/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.AddressableLEDBuffer;

/**
 * Manager class for LEDs, providing different patterns to use in a AddressableLEDBuffer.
 */
public class OCLedManager {
    
    public enum States {
        Idle(OCLedManager::idle),
        Shooting(OCLedManager::shooting); // etc

        private Runnable pattern;// This just points to the function that creates a certain effect
        States(Runnable pattern){
            this.pattern = pattern;
        }

        public Runnable getPattern(){
            return pattern;
        }
    }

    private static States currState = States.Idle;
    private static AddressableLEDBuffer buffer;

    public static void setBuffer(AddressableLEDBuffer buff){ // The class requires a buffer to change
        buffer = buff;
    }

    public static States getState(){
        return currState;
    }
    public static void setState(States state){
        currState = state;
    }

    public static void periodic(){
        if(buffer != null) currState.getPattern().run(); // this just calls the current function
    }

    // these functions modify the buffer to create patterns
    private static void idle(){
        for(int i=0;i<buffer.getLength();i++){
            buffer.setHSV(i, 10, 200, 150);
        }
    }
    private static void shooting(){
        for(int i=0;i<buffer.getLength();i++){
            buffer.setHSV(i, 100, 200, 150);
        }
    }
}
