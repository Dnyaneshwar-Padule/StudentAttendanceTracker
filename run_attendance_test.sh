#!/bin/bash

# Compile the project
echo "Compiling the project..."
mvn compile

# Run the test class
echo "Running AttendanceDAO tests..."
mvn exec:java -Dexec.mainClass="com.attendance.tests.SimpleAttendanceTest"

echo "Test execution completed"