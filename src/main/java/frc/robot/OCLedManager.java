/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Units;
import edu.wpi.first.wpiutil.math.MathUtil;

/**
 * Manager class for LEDs, providing different patterns to use in a AddressableLEDBuffer.
 */
public class OCLedManager {
    
    public enum States {
        Idle(OCLedManager::idle),
        Shooting(OCLedManager::shooting),
        Wave(OCLedManager::wave);
        // etc
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

    private static double hue = 0;

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

    private static int[] rgbWave(int hue, int sat, int val){
        sat = 255-sat;
        int[] rgb = {sat + val*(hue)};
        return rgb;
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
    private static void wave(){
        final int hueRange = 45;
        final int hueInitial = 100;
        for(var i=0;i<buffer.getLength();i++){
            //final var currHue = ((int)hue + (i*hueRange / buffer.getLength())) % hueRange; 
            final int currrHue = (int)(((Math.sin(hue/180*Math.PI)+1) * (hueRange/2.0) + (i*hueRange/buffer.getLength())) % hueRange);
            SmartDashboard.putNumber("hue", currrHue+hueInitial);
            buffer.setHSV(i, currrHue+hueInitial, 255, 160);
        }
        hue = Timer.getFPGATimestamp()*80;

        hue %= 360;
    }
}
