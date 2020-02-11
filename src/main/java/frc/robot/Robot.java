/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.OCLedManager.Pattern;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  AddressableLED led = new AddressableLED(1);
  AddressableLEDBuffer ledBuffer = new AddressableLEDBuffer(120);
  SendableChooser<Pattern> patternChooser = new SendableChooser<>();

  DigitalInput test = new DigitalInput(0);

  public Robot(){
    super(0.04); // Smooth <= 0.03, gets slightly more chunky past that(test with progress bar)
  }

  @Override
  public void robotInit() {
    led.setLength(ledBuffer.getLength());
    led.start();

    OCLedManager.setBuffer(ledBuffer);
    OCLedManager.setEffectiveLength(45);

    patternChooser.setDefaultOption(Pattern.Slideshow.toString(), Pattern.Slideshow);
    for(Pattern state:OCLedManager.Pattern.values()){
      patternChooser.addOption(state.toString(), state);
    }
    SmartDashboard.putData(patternChooser);
  }

  @Override
  public void robotPeriodic() {
    OCLedManager.setPattern(patternChooser.getSelected());
    OCLedManager.periodic();
    led.setData(ledBuffer);
    SmartDashboard.putBoolean("Beam", test.get());
  }

  @Override
  public void autonomousInit() {
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
  }

  @Override
  public void teleopPeriodic() {
  }

  @Override
  public void testInit() {
  }

  @Override
  public void testPeriodic() {
  }

}
