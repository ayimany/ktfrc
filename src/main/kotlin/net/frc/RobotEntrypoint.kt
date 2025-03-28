@file:Suppress("RedundantOverride")

package net.frc

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj2.command.CommandScheduler

class RobotEntrypoint : TimedRobot() {

    //
    // Robot
    //

    override fun robotInit() {
        super.robotInit()
        DriverStation.silenceJoystickConnectionWarning(true)
    }

    override fun robotPeriodic() {
        super.robotPeriodic()
        runScheduler()
    }

    //
    // Teleop
    //

    override fun teleopInit() {
        super.teleopInit()
    }

    override fun teleopPeriodic() {
        super.teleopPeriodic()
    }

    override fun teleopExit() {
        super.teleopExit()
    }

    //
    // Autonomous
    //

    override fun autonomousInit() {
        super.autonomousInit()
    }

    override fun autonomousPeriodic() {
        super.autonomousPeriodic()
    }

    override fun autonomousExit() {
        super.autonomousExit()
    }

    //
    // Disabled
    //

    override fun disabledInit() {
        super.disabledInit()
        cancelAllCommands()
    }

    override fun disabledPeriodic() {
        super.disabledPeriodic()
    }

    override fun disabledExit() {
        super.disabledExit()
    }

    //
    // Test
    //

    override fun testInit() {
        super.testInit()
    }

    override fun testPeriodic() {
        super.testPeriodic()
        cancelAllCommands()
    }

    override fun testExit() {
        super.testExit()
    }

    //
    // Helpers
    //

    fun runScheduler() = CommandScheduler.getInstance().run()
    fun cancelAllCommands() = CommandScheduler.getInstance().cancelAll()

}