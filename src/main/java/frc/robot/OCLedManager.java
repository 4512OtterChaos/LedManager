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
        Green(()->solid(green, 255, 200)),
        Red(()->solid(red, 255, 200)),
        AutomaticWave(OCLedManager::automaticWave),
        BlueWave(OCLedManager::rollingBlueWave),
        RedWave(OCLedManager::rollingRedWave),
        YellowDash(()->dashes(9,10,30,5)),
        ProgressBar(()->progressBar(yellow, (Timer.getFPGATimestamp()*0.5)%1)),
        Tide(()->tide(blue, 0)),
        BluePulsing(()->pulsing(blue, 255, 10)),
        RedPulsing(()->pulsing(red, 255, 10)),
        RockingWave(()->rockingWave(180, 0)),
        Random(OCLedManager::random),
        Hmmm(()->pulsing(60, 255, 10)),
        Hmmv2(OCLedManager::doot);
        // etc
        private final Runnable pattern;// This just points to the function that creates a certain effect

        Pattern(final Runnable pattern) {
            this.pattern = pattern;
        }

        public Runnable getPattern() {
            return pattern;
        }
    }

    private static Pattern currPattern = Pattern.Slideshow;
    private static AddressableLEDBuffer buffer;

    // Persistent variables to change over time
    private static double workingHue = 0;
    private static double workingSat = 0;
    private static double workingVal = 0;
    private static boolean drawingDash = false;
    private static double lastTime = Timer.getFPGATimestamp();

    // Constants affecting appearance
    private static final int green = 60;
    private static final int red = 0;
    private static final int blue = 108;
    private static final int yellow = 30;

    private static final int waveLength = 16; // size of waves
    private static final int waveThresholdValue = 100; // minimum wave brightness

    public static void setBuffer(final AddressableLEDBuffer buff) { // The class requires a buffer to change
        buffer = buff;
    }

    public static Pattern getPattern() {
        return currPattern;
    }

    public static void setPattern(final Pattern pattern) {
        if (pattern == null)
            currPattern = Pattern.Slideshow;
        else if (!pattern.equals(currPattern))
            currPattern = pattern;
    }

    public static void periodic() {
        if (buffer != null)
            currPattern.getPattern().run(); // this just calls the relevant function
    }

    // ----Helper methods
    /**
     * Finds the continuous error between two pixel indexes.
     * 
     * @param a
     * @param b
     * @param length
     * @return
     */
    private static int findDifference(final int a, final int b, final int length) {
        return Math.abs(Math.abs(a - b + length / 2) % length - length / 2);
    }
    // -----

    // vvv These functions modify the buffer to create patterns vvv

    // -----Basic patterns
    /**
     * Sets all pixels to parameters
     */
    private static void solid(final int hue, final int sat, final int val) {
        for (int i = 0; i < buffer.getLength(); i++) {
            buffer.setHSV(i, hue, sat, val);
        }
    }

    /**
     * Fills strip with pixels of hue to percent amount
     */
    private static void progressBar(final int hue, final double percentage) {
        MathUtil.clamp(percentage, 0, 1);
        solid(0, 0, 0);
        for (var i = 0; i < (int) (buffer.getLength() * percentage); i++) {
            buffer.setHSV(i, hue, 255, 255);
        }
    }

    /**
     * Pulses the strip with hue and saturation at speed
     */
    private static void pulsing(final int hue, final int saturation, final int speed) {
        final int value = (int) (((Math.sin(Timer.getFPGATimestamp() * speed)) + 1) * (255 / 2.0));
        for (var i = 0; i < buffer.getLength(); i++) {
            buffer.setHSV(i, hue, saturation, value);
        }

    }

    /**
     * Draws a moving dashed line.
     * 
     * @param gap    Pixels between dashes
     * @param length Pixel length of dashes
     */
    private static void dashes(final int hue, final int gap, final int speed, final int length) {
        int currGap = gap;
        int currLength = length;
        final int offset = (int) (Timer.getFPGATimestamp() * speed % buffer.getLength());
        int currIndex;
        for (var i = 0; i < buffer.getLength(); i++) {
            // this makes offset
            currIndex = (i + offset) % buffer.getLength();
            if (!drawingDash) {
                currLength = length;
                buffer.setHSV(currIndex, 0, 0, 0);
                currGap--;
                if (currGap == 0)
                    drawingDash = true;
            } else if (drawingDash) {
                currGap = gap;
                buffer.setHSV(currIndex, hue, 255, 255);
                currLength--;
                if (currLength == 0)
                    drawingDash = false;
            }
        }
    }

    /**
     * Creates a moving hotspot that scrolls along the strip.
     * 
     * @param initOffset Starting pixel
     */
    private static void hotspot(final int initOffset, final int speed, final int hue, final int sat) {
        final int offset = (int) (Timer.getFPGATimestamp() * speed % buffer.getLength() + initOffset);
        for (var i = 0; i < buffer.getLength(); i++) {
            final int difference = findDifference(offset, i, buffer.getLength());
            int value = (int) (255 - difference * (2 * (255 - waveThresholdValue) / (waveLength)));
            value = Math.max(waveThresholdValue, value);
            if (value > waveThresholdValue)
                buffer.setHSV(i, hue, sat, value);
        }
    }
    // -----

    // -----Specific Patterns
    private static void slideshow() {// cycle through all patterns every 6 seconds
        final int patternIndex = (int) ((Timer.getFPGATimestamp() / 6) % (Pattern.values().length - 1));
        Pattern.values()[patternIndex + 1].getPattern().run();
    }

    private static void rollingBlueWave() {
        solid(blue, 255, waveThresholdValue);
        hotspot(0, 20, blue, 255);
        hotspot(buffer.getLength() / 3, 20, blue, 220);
        hotspot((buffer.getLength() / 3) * 2, 20, blue, 190);
    }

    private static void rollingRedWave() {
        solid(red, 255, waveThresholdValue);
        hotspot(0, 20, red, 255);
        hotspot(buffer.getLength() / 3, 20, red + 2, 255);
        hotspot((buffer.getLength() / 3) * 2, 20, red + 4, 255);
    }

    private static void automaticWave() {
        if (DriverStation.getInstance().getAlliance() == Alliance.Red) {
            rollingRedWave();
        } else {
            rollingBlueWave();
        }
    }

    private static void tide(final int hue, final int satMin) {
        for (int i = 0; i < buffer.getLength(); i++) {
            final int currSat = (int) (workingSat + (i * (255 - satMin) / buffer.getLength())) % 256;
            buffer.setHSV(i, hue, currSat + satMin, 255);
        }
        workingSat = (int) (Timer.getFPGATimestamp() * 80) % 256;
    }

    private static void rockingWave(final int hueRange, final int hueInitial) {
        for (var i = 0; i < buffer.getLength(); i++) {
            final int currHue = (int) (((Math.sin(workingHue / 180 * Math.PI) + 1) * (hueRange / 2.0)
                    + (i * hueRange / buffer.getLength())) % hueRange);
            buffer.setHSV(i, currHue + hueInitial, 255, 255);
        }
        workingHue = (int) (Timer.getFPGATimestamp() * 80);

        workingHue %= 360;
    }

    private static void seaHawks() {
        for (int i = 0; i < buffer.getLength(); i += 2) {
            buffer.setHSV(i, 105, 225, 61);
            buffer.setHSV(i + 1, 47, 178, 169);
        }

    }

    private static void matrix() {
        final int offset = (int) (Timer.getFPGATimestamp() * 20);
        for(var i = 0;i < buffer.getLength();i++){
            buffer.setHSV((i+offset)%buffer.getLength(), 1+(int)(i * Math.pow(Math.sin(1.2*i), 1.8)), 255, 255);
        }
    }
   private static void random(){
    int length = 5;
    double interval = 0.05;
    /*
    for(int i=0;i<buffer.getLength();i++){
        buffer.setHSV(i,(int)(Math.random()*180), 255, 255);
    }*/

    if(Timer.getFPGATimestamp()>lastTime+interval){
        for(int i=0;i<buffer.getLength()/length;i++){
            int hue = (int)(Math.random()*180);
            for(int j=0;j<length;j++){
                buffer.setHSV(i*length+j, hue, 255, 255);
            }
        }
        lastTime=Timer.getFPGATimestamp();
    }
   }

   private static void doot(){
    int length = 5;
    double interval = 0.05;

    if(Timer.getFPGATimestamp()>lastTime+interval){
        for(int i=0;i<buffer.getLength()/length;i++){
            int hue = 74;
            for(int j=0;j<length;j++){
                buffer.setHSV(i, hue, 255, 255);

            }
        }
        lastTime=Timer.getFPGATimestamp();    
    }
  }
}