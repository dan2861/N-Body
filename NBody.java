    
//*******************************************************************
//
//   File: NBody.java
//
//   Author: dym7      Email: danu.metaferia@yale.edu
//
//   Class: NBody 
// 
//   Time spent on this problem: 6hrs
//   --------------------
//   
//      This program takes in a the duration of the animation, the time
//      step and a universe file, then it simulates the motion of N objects 
//      governed by real world physics. Each object affects the motion of 
//      the other and the speed at which the animation goes is controlled
//      with a slider. The program draws using multiple files including 
//      .jpg, .txt, and audio files.
//
//*******************************************************************
import java.util.Observer;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.IOException;

public class NBody {

    // set up panel
    static final int SIZE = 500;
    static DrawingPanel panel = new DrawingPanel(SIZE, SIZE);
    static Graphics2D g = panel.getGraphics();

    // enable double buffering
    static BufferedImage offscreen = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
    static Graphics2D osg = offscreen.createGraphics();

    // slider position
    public static final int slideStart = 100;
    public static final int slideEnd = 400;

    public static int slideX = 250;
    public static final int slideY = 450;
    public static final int circleRad = 10;

    // animation pause (in miliseconds)
    public static final int DELAY = 100;

    // music (2001 theme)
    public static final String MUSIC = "2001theme.wav";

    // background image
    public static final String BACKGROUND = "starfield.jpg";
    public static BufferedImage bgImage;

    // gravitational constant (N m^2 / kg^2)
    public static final double G = 6.67e-11;

    // parameters from command line
    public static double simDuration; // simulate from time 0 to simDuration (s)
    public static double baseTimeStep; // time quantum given by user input
    public static double timeStep; // time quantum used in simulation. can be updated by speed slider

    // parameters from .txt file
    public static int numBodies; // number of bodies (N)
    public static double universeRadius; // radius of universe (R)

    public static double[] rx; // x position (m)
    public static double[] ry; // y position (m)
    public static double[] vx; // x velocity (m/s)
    public static double[] vy; // y velocity (m/s)
    public static double[] mass; // mass (kg)
    public static String[] imageNames; // image file names
    public static BufferedImage[] images; // image objects

    public static void main(String[] args) {

        // check for number of arguments, give usage string
        if (args.length < 3) {
            printUsage();
            System.exit(1);
        }

        simDuration = Double.parseDouble(args[0]);

        baseTimeStep = Double.parseDouble(args[1]);
        timeStep = baseTimeStep;

        String universeFileName = args[2];

        // load BACKGROUND image
        loadBG();

        // load planets from file specified in the command line
        loadPlanets(universeFileName);

        System.out.printf("%d\n", numBodies);
        System.out.printf("%.2e\n", universeRadius);
        
        // play audio file
        StdAudio.play(MUSIC);
        
        // Set up mouse listener for slider
        panel.onDrag((x, y) -> dragUpdate(x, y));

        // Run simulation
        runSimulation();

        // print final state of universe to standard output
        System.out.printf("%d\n", numBodies);
        System.out.printf("%.2e\n", universeRadius);
        for (int i = 0; i < numBodies; i++) {
            System.out.printf("%11.4e %11.4e %11.4e %11.4e %11.4e %12s\n",
                    rx[i], ry[i], vx[i], vy[i], mass[i], imageNames[i]);
        }
    }

    public static void printUsage() {
        System.out.println("Usage:");
        System.out.println("java NBody <duration> <time step> <universe file>");
    }

    // load BACKGROUND image into the variable bgImage
    public static void loadBG() {
        // Catch the exception thrown 
        // when opening a file
        try {
            // Read image
            bgImage = ImageIO.read(new File(BACKGROUND));

        } catch (IOException e) {
            // Print the line where the error occurred
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Read the planet file, create the parallel arrays, and load
    // their values from the file.
    public static void loadPlanets(String planetFileName) {
        Scanner scan;
        // Catch the exceptions thrown
        try {
            // Open the file
            scan = new Scanner(new File(planetFileName));
            numBodies = scan.nextInt();
            universeRadius = scan.nextDouble();

            // Create the planet state arrays
            rx = new double[numBodies];
            ry = new double[numBodies];
            vx = new double[numBodies];
            vy = new double[numBodies];
            mass = new double[numBodies];
            imageNames = new String[numBodies];
            images = new BufferedImage[numBodies];

            // Populate the arrays with the N-bodies info
            for(int i = 0; i < numBodies; i++) {
                rx[i] = scan.nextDouble();
                ry[i] = scan.nextDouble();
                vx[i] = scan.nextDouble();
                vy[i] = scan.nextDouble();
                mass[i] = scan.nextDouble();
                imageNames[i] = scan.next();
                images[i] = ImageIO.read(new File(imageNames[i]));
            }   // End of for loop
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void runSimulation() {
        // run numerical simulation from 0 to simDuration
        // speed may vary if the slider updates timeStep
        for (double t = 0.0; t < simDuration; t += timeStep) {

            // the x- and y-components of force
            double[] fx = new double[numBodies];
            double[] fy = new double[numBodies];

            // loop to initialize fx and fy
            for(int i = 0; i < numBodies; i++) {
                fx[i] = 0.0;
                fy[i] = 0.0;
            }

            // calculate forces on each object
            for(int i = 0; i < numBodies; i++) {
                for(int j = 0; j < numBodies; j++) {
                    // Checks the planets aren't the same
                    if(i !=j) {
                        // x-component of position
                        double dx = rx[j] - rx[i];
                        // y-component of position
                        double dy = ry[j] - ry[i];
                        // The distance between the two bodies
                        double r = Math.sqrt(dx * dx + dy * dy);
                        // Force of gravity
                        double F = (G * mass[i] * mass[j])/ (r * r);

                        // Computes forces in the x & y components
                        fx[i] += F * (dx/r);
                        fy[i] += F * (dy/r);
                    } 
                }   // End of inner for loop
            }   // End of outer for loop

            // Compute the velocity and positon
            for(int i = 0; i < numBodies; i++) {
                // Calculates the acceleration at time timestep
                double ax = fx[i] / mass[i];
                double ay = fy[i] / mass[i];
                
                // Calculates the velocity at time timestep
                vx[i] += timeStep * ax;
                vy[i] += timeStep * ay;

                // Calculates the position at time timestep
                rx[i] += timeStep * vx[i];
                ry[i] += timeStep * vy[i];
            }

            // draw background
            osg.drawImage(bgImage, 0, 0, SIZE, SIZE, null);
            
            // draw each planet
            for(int i = 0; i < numBodies; i++) {
                // Scale the x and y components to panel
                double scaledX = scaleToPanel(rx[i]);
                double scaledY = SIZE - scaleToPanel(ry[i]);

                // Align image center with the  body's coordinate.
                double pivotX = scaledX - images[i].getWidth() / 2;
                double pivotY = scaledY - images[i].getHeight() / 2;

                // Draw image off screen
                osg.drawImage(images[i], null, round(pivotX),  round(pivotY));
            }

            // draw slider 
            drawSlider();
            
            // copy from offscreen buffer to panel
            g.drawImage(offscreen, 0, 0, null);

            // pause
            panel.sleep(DELAY);
        }
    }

    // general scaling method
    public static double scale(double oldValue, double oldMin, double oldMax, double newMin, double newMax) {
        double newValue = ((oldValue - oldMin) / (oldMax - oldMin)) * (newMax - newMin) + newMin;
        return newValue;
    }

    // Scales the universe to panel size
    public static int scaleToPanel(double oldValue) {
        return (int) (scale(oldValue, -universeRadius, universeRadius, 0, SIZE));
    }

    // handle mouse events
    public static void dragUpdate(int x, int y) {
        // Distance between the center of the circle and y squared
        int dxSquared = (x - slideX) * (x - slideX);
        // Distance between the center of the circle and y squared
        int dySquared = (y - slideY) * (y - slideY);
        // Radius of the circle squared
        int circleRadSquared = circleRad * circleRad;

        // Using standard equation of a circle, 
        // checks if slider is inside the circle
        boolean inCircle = (dxSquared + dySquared <= circleRadSquared);

        // Updates slider when slider circle is grabbed
        if(inCircle) {
            // Limits slider between the start and end of slider
            if(x <= slideStart) {
                slideX = slideStart;
            } else if(x >= slideEnd) {
                slideX = slideEnd;
            } else {
                slideX = x;
            }
        }

        // and update time step size based on slider position
        timeStep = scale(slideX, slideStart, slideEnd, 0.0 * baseTimeStep, 2.0 * baseTimeStep);
    }

    // draw the slider for this frame
    public static void drawSlider() {

        osg.setColor(Color.GRAY);

        // dashed slider line
        osg.drawLine(slideStart, slideY, slideEnd, slideY);
        for (int dashX = slideStart; dashX <= slideEnd; dashX += (slideEnd - slideStart) / 12) {
            osg.drawLine(dashX, slideY - 5, dashX, slideY + 5);
        }

        for (int dashX = slideStart; dashX <= slideEnd; dashX += (slideEnd - slideStart) / 4) {
            osg.drawLine(dashX, slideY - 10, dashX, slideY + 10);
        }

        // outlined slider "button"
        osg.fillOval(slideX - circleRad, slideY - circleRad, 2 * circleRad, 2 * circleRad);
        osg.setColor(Color.WHITE);
        osg.drawOval(slideX - circleRad, slideY - circleRad, 2 * circleRad, 2 * circleRad);
    }

    // round a double to the nearest int
    public static int round(double d) {
        return (int) Math.round(d);
    }
}