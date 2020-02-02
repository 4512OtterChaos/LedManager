/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.sql.Driver;

import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Units;
import edu.wpi.first.wpiutil.math.MathUtil;

/**
 * Manager class for LEDs, providing different patterns to use in a AddressableLEDBuffer.
 */
public class OCLedManager {
    
    public enum Pattern {
        Idle(OCLedManager::idle),
        Shooting(OCLedManager::shooting),
        Wave(OCLedManager::wave),
        Copypasta(()->copypasta(255)),
        Green(OCLedManager::green),
        Red(OCLedManager::red),
        ColorDash(OCLedManager::colorDash),
        RollingBlueWave(OCLedManager::rollingBlueWave),
        AllWhite(OCLedManager::allWhite),
        RollingRedWave(OCLedManager::rollingRedWave),
        RollPink(OCLedManager::pink),
        Automatic(OCLedManager::automatic),
        ProgressBar(()->progressBar((Timer.getFPGATimestamp()*0.5)%1)),
        Pulsing(()->pulsing(blue,255,10));
        

        // etc
        private Runnable pattern;// This just points to the function that creates a certain effect
        Pattern(Runnable pattern){
            this.pattern = pattern;
        }

        public Runnable getPattern(){
            return pattern;
        }
    }

    private static Pattern currPattern = Pattern.Idle;
    private static AddressableLEDBuffer buffer;

    private static double hue = 0;
    private static double sat = 0;
    private static boolean drawingDash = false;
    private static final int green = 60;
    private static final int red = 0;
    private static final int blue = 108;
    private static final int yellow = 25;
    private static final int waveLength = 30;
    private static final int waveThresholdValue = 25;


    public static void setBuffer(AddressableLEDBuffer buff){ // The class requires a buffer to change
        buffer = buff;
    }

    public static Pattern getPattern(){
        return currPattern;
    }
    public static void setState(Pattern pattern){
        if(pattern == null) currPattern=Pattern.Idle;
        else if(!pattern.equals(currPattern)) currPattern=pattern;
    }

    public static void periodic(){
        if(buffer != null) currPattern.getPattern().run(); // this just calls the current function
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

    private static void copypasta(int value){
        final int satRange = 213;
        final int satInitial = 42;
        for(var i=0;i<buffer.getLength();i++){
            final int currSat = (int)((sat+(i*satRange / buffer.getLength())) % satRange);
            SmartDashboard.putNumber("currSat", currSat+satInitial);
            buffer.setHSV(i, blue, currSat+satInitial, value);
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

    private static void yellowDash(int gap, int speed, int length){
        int currGap = gap;
        int currLength = length;
        int offset = (int)(Timer.getFPGATimestamp()*speed % buffer.getLength());
        int currIndex;
        for (var i=0;i<buffer.getLength();i++){
            //this makes offset 
            currIndex = (i + offset)%buffer.getLength();
            if(!drawingDash){
                currLength = length;
                buffer.setHSV(currIndex, 0, 0, 0);
                currGap--;
                if(currGap == 0) drawingDash = true;
            }
            else if(drawingDash) {
                currGap=gap;
                buffer.setHSV(currIndex, 9, 255, 255);
                currLength--;
                if(currLength == 0) drawingDash = false;
            }


        }

    }
    
    private static void colorDash(){
        green();
        yellowDash(10, 30, 5);

    }

    private static void rollingBlueWave(){
        //allWhite();
        rollBlue(0, 255);
        rollBlue(buffer.getLength()/3, 170);
        rollBlue((buffer.getLength()/3)*2, 110);
    }

    private static void allWhite(){
        for (var i=0;i<buffer.getLength();i++){
            buffer.setHSV(i, 0, 0, waveThresholdValue);
        }
    }

    private static void rollBlue(int initOffset, int sat){
        int offset = (int)(Timer.getFPGATimestamp()*20)%buffer.getLength()+(buffer.getLength())/3 + initOffset;
        for (var i=0;i<buffer.getLength();i++){
            int difference = findDifference(offset, i, buffer.getLength());
            difference = Math.min(difference, 255/waveLength+1);
            int value =(254-difference*waveLength)%255;
            value = value < waveThresholdValue ? 0:value;
            if(value>0) buffer.setHSV(i, blue, sat, value);
        }
    }

    private static void rollingRedWave(){
        rollRed(0, 0);
        rollRed(buffer.getLength()/3, 2);
        rollRed((buffer.getLength()/3)*2, 4);
    }



    private static void rollRed(int initOffset, int hue){
        int offset = (int)(Timer.getFPGATimestamp()*20%buffer.getLength() + initOffset);
        for (var i=0;i<buffer.getLength();i++){
            int difference = findDifference(offset, i, buffer.getLength());
            difference = Math.min(difference, 255/waveLength+1);
            int value =(254-difference*waveLength)%255;
            value = value < waveThresholdValue ? 0:value;
            if(value>0) buffer.setHSV(i, hue, 255, value);
        }
    }
    
    private static void automatic(){
        if(DriverStation.getInstance().getAlliance()==Alliance.Red){
            rollingRedWave();
        } 
        else{
            rollingBlueWave();
        }
    }


    /**
     * Finds the continuous error between two pixel indexes.
     * @param a
     * @param b
     * @param length
     * @return
     */
    private static int findDifference(int a, int b, int length){
        return Math.abs(Math.abs(a-b+length/2)%length-length/2);
    }
    private static void pink(){
        int offset = (int)(Timer.getFPGATimestamp()*20)%buffer.getLength()+(buffer.getLength())/3;
        for (var i=0;i<buffer.getLength();i++){
            int difference = findDifference(offset, i, buffer.getLength());
            difference = Math.min(difference, 255/waveLength+1);
            int value =(254-difference*waveLength)%255;
            value = value < waveThresholdValue ? 0:value;
            if(value>0) buffer.setHSV(i, 157, 255, value);
        }
    }
    
    private static void progressBar(double percentage){
        MathUtil.clamp(percentage, 0, 1);
        red();
        for (var i = 0; i < (int)(buffer.getLength()*percentage); i++) {
            buffer.setHSV(i, yellow, 255, 255);
        } 



    }

    
    private static void pulsing(int hue, int saturation, int speed){
        int value = (int)(((Math.sin(Timer.getFPGATimestamp()*speed))+1)*(255/2.0));
        for (var i=0;i<buffer.getLength();i++){
          buffer.setHSV(i, hue, saturation, value);
        }
        
    }
    


}
