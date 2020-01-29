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
        Wave(OCLedManager::wave),
        Copypasta(OCLedManager::copypasta),
        Green(OCLedManager::green),
        Red(OCLedManager::red),
        YellowDash(OCLedManager::yellowDash);
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
    private static double sat = 0;
    private static int green = 65;
    private static int red = 2;
    private static int blue = 108;
    private static int yellow = 35;


    public static void setBuffer(AddressableLEDBuffer buff){ // The class requires a buffer to change
        buffer = buff;
    }

    public static States getState(){
        return currState;
    }
    public static void setState(States state){
        if(!state.equals(currState)) currState=state;
    }

    public static void periodic(){
        if(buffer != null) currState.getPattern().run(); // this just calls the current function
    }

    // these functions modify the buffer to create patterns
    private static void idle(){
        for(int i=0;i<buffer.getLength();i++){
            buffer.setHSV(i, 8, 200, 150);
        }
    }
    private static void shooting(){
        for(int i=0;i<buffer.getLength();i++){
            buffer.setHSV(i, 100, 200, 150);
        }
    }
    private static void wave(){
        final int hueRange = 100;
        final int hueInitial = 80;
        for(var i=0;i<buffer.getLength();i++){
            //final var currHue = ((int)hue + (i*hueRange / buffer.getLength())) % hueRange; 
            final int currrHue = (int)(((Math.sin(hue/180*Math.PI)+1) * (hueRange/2.0) + (i*hueRange/buffer.getLength())) % hueRange);
            SmartDashboard.putNumber("hue", currrHue+hueInitial);
            buffer.setHSV(i, currrHue+hueInitial, 255, 100);
        }
        hue = (int)(Timer.getFPGATimestamp()*80);

        hue %= 360;
    }

    private static void copypasta(){
        final int satRange = 213;
        final int satInitial = 42;
        for(var i=0;i<buffer.getLength();i++){
            final int currSat = (int)((sat+(i*satRange / buffer.getLength())) % satRange);
            SmartDashboard.putNumber("currSat", currSat+satInitial);
            buffer.setHSV(i, blue, currSat+satInitial, 255);
        }
        sat = (int)(Timer.getFPGATimestamp()*80);

        sat %= 255;
        SmartDashboard.putNumber("sat", sat);
    }
    private static void green(){
        for (var i = 0; i < buffer.getLength(); i++) {

            buffer.setHSV(i, green, 255, 255);
        } 

    }
    private static void red(){
        for (var i = 0; i < buffer.getLength(); i++) {

            buffer.setHSV(i, red, 255, 255);
        } 

    }

    private static void yellowDash(){

        for (var i=0;i<buffer.getLength();i++) {
            
            if(i%2==0){
                buffer.setHSV(i, yellow, 255, 255);
            }
        }

    }
    



}
