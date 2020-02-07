/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;



import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpiutil.math.MathUtil;

/**
 * Manager class for LEDs, providing different patterns to use in a AddressableLEDBuffer.
 */
public class OCLedManager {
    
    public enum Pattern {
        Slideshow(OCLedManager::slideshow),
        Wave(OCLedManager::wave),
        Copypasta(()->copypasta(255)),
        Green(()->solid(green, 255, 200)),
        Red(()->solid(red, 255, 200)),
        YellowDash(()->dashes(9,10,30,5)),
        RollingBlueWave(OCLedManager::rollingBlueWave),
        RollingRedWave(OCLedManager::rollingRedWave),
        RollPink(()->hotspot(0, 20, 157, 255)),
        IdleWave(OCLedManager::idleWave),
        ProgressBar(()->progressBar((Timer.getFPGATimestamp()*0.3)%1)),
        BluePulsing(()->pulsing(blue, 255, 20)),
        RedPulsing(()->pulsing(red, 255, 20)),
        Seahawks(OCLedManager::seaHawks),
        Matrix(OCLedManager::matrix);
        // etc
        private Runnable pattern;// This just points to the function that creates a certain effect
        Pattern(Runnable pattern){
            this.pattern = pattern;
        }

        public Runnable getPattern(){
            return pattern;
        }
    }

    private static Pattern currPattern = Pattern.Slideshow;
    private static AddressableLEDBuffer buffer;

    // Persistent variables to change over time
    private static double workingHue = 0;
    private static double workingSat = 0;
    private static boolean drawingDash = false;

    // Constants affecting appearance
    private static final int green = 60;
    private static final int red = 0;
    private static final int blue = 108;
    private static final int yellow = 30;

    private static final int waveLength = 30; // size of waves
    private static final int waveThresholdValue = 25; // minimum wave brightness


    public static void setBuffer(AddressableLEDBuffer buff){ // The class requires a buffer to change
        buffer = buff;
    }

    public static Pattern getPattern(){
        return currPattern;
    }
    public static void setState(Pattern pattern){
        if(pattern == null) currPattern=Pattern.Slideshow;
        else if(!pattern.equals(currPattern)) currPattern=pattern;
    }

    public static void periodic(){
        if(buffer != null) currPattern.getPattern().run(); // this just calls the relevant function
    }

    //----Helper methods
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

    /**
     * Sets all pixels to parameters
     * @param hue
     * @param sat
     * @param val
     */
    private static void solid(int hue, int sat, int val){
        for(int i=0;i<buffer.getLength();i++){
            buffer.setHSV(i, hue, sat, val);
        }
    }

    /**
     * Creates a moving hotspot that scrolls along the strip.
     * @param initOffset Starting pixel
     * @param speed Rate of scroll
     * @param hue
     * @param sat
     */
    private static void hotspot(int initOffset, int speed, int hue, int sat){
        int offset = (int)(Timer.getFPGATimestamp()*speed%buffer.getLength() + initOffset);
        for (var i=0;i<buffer.getLength();i++){
            int difference = findDifference(offset, i, buffer.getLength());
            difference = Math.min(difference, 255/waveLength+1);
            int value =(254-difference*waveLength)%255;
            value = value < waveThresholdValue ? waveThresholdValue:value;
            if(value>waveThresholdValue) buffer.setHSV(i, hue, 255, value);
        }
    }
    //----

    // vvv These functions modify the buffer to create patterns vvv

    private static void slideshow(){// cycle through all patterns every 5 seconds
        int patternIndex = (int)((Timer.getFPGATimestamp()/5)%(Pattern.values().length-1));
        Pattern.values()[patternIndex+1].getPattern().run();
    }

    private static void wave(){
        final int hueRange = 100;
        final int hueInitial = 80;
        for(var i=0;i<buffer.getLength();i++){
            int currrHue = (int)(((Math.sin(workingHue/180*Math.PI)+1) * (hueRange/2.0) + (i*hueRange/buffer.getLength())) % hueRange);
            SmartDashboard.putNumber("Hue", currrHue+hueInitial);
            buffer.setHSV(i, currrHue+hueInitial, 255, 100);
        }
        workingHue = (int)(Timer.getFPGATimestamp()*80);

        workingHue %= 360;
    }

    private static void copypasta(int value){
        final int satRange = 213;
        final int satInitial = 42;
        for(var i=0;i<buffer.getLength();i++){
            int currSat = (int)((workingSat+(i*satRange / buffer.getLength())) % satRange);
            currSat+=satInitial;
            buffer.setHSV(i, blue, currSat, value);
        }
        workingSat = (int)(Timer.getFPGATimestamp()*80);

        workingSat %= 255;
        SmartDashboard.putNumber("workingSat", workingSat);
    }

    /**
     * Draws a moving dashed line.
     * @param hue 
     * @param gap Pixels between dashes
     * @param speed 
     * @param length Pixel length of dashes
     */
    private static void dashes(int hue, int gap, int speed, int length){
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
                buffer.setHSV(currIndex, hue, 255, 255);
                currLength--;
                if(currLength == 0) drawingDash = false;
            }
        }
    }



    private static void rollingBlueWave(){
        /*
        solid(0,0,25);
        rollBlue(0, 255);
        rollBlue(buffer.getLength()/3, 170);
        rollBlue((buffer.getLength()/3)*2, 110);
        */

        solid(0, 0, waveThresholdValue);
        hotspot(0, 20, blue, 255);
        hotspot(buffer.getLength()/3, 20, blue, 220);
        hotspot((buffer.getLength()/3)*2, 20, blue, 190);
    }

    private static void rollingRedWave(){
        /*
        rollRed(0, red);
        rollRed(buffer.getLength()/3, 2);
        rollRed((buffer.getLength()/3)*2, 4);
        */

        solid(red, 255, waveThresholdValue);
        hotspot(0, 20, red, 255);
        hotspot(buffer.getLength()/3, 20, red+2, 255);
        hotspot((buffer.getLength()/3)*2, 20, red+4, 255);
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
    
    private static void idleWave(){
        if(DriverStation.getInstance().getAlliance()==Alliance.Red){
            rollingRedWave();
        } 
        else{
            rollingBlueWave();
        }
    }

    private static void seaHawks() {
        for (int i = 0; i < buffer.getLength(); i+= 2){
            buffer.setHSV(i, 105, 225, 61);
            buffer.setHSV(i + 1, 47, 178, 169);
        }

    }
    
    private static void progressBar(double percentage){
        MathUtil.clamp(percentage, 0, 1);
        solid(0,0,0);
        for (var i = 0; i < (int)(buffer.getLength()*percentage); i++) {
            buffer.setHSV(i, 30-(int)(i/3.5), 255, 255);
        } 
    }
    
    private static void pulsing(int hue, int saturation, int speed){
        int value = (int)(((Math.sin(Timer.getFPGATimestamp()*speed))+1)*(255/2.0));
        for (var i=0;i<buffer.getLength();i++){
          buffer.setHSV(i, hue, saturation, value);
        }
        
    }

    private static void matrix(){
        int offset = (int)(Timer.getFPGATimestamp()*20);
        for(var i = 0;i < buffer.getLength();i++){
            buffer.setHSV((i+offset)%buffer.getLength(), 1+(int)(i * Math.pow(Math.sin(1.2*i), 1.8)), 255, 255);
        }
    }
}