package com.attendance.utils;

import com.attendance.models.BiometricData;

import org.opencv.core.*;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for face recognition using OpenCV
 */
public class FaceRecognitionService {
    private static final Logger LOGGER = Logger.getLogger(FaceRecognitionService.class.getName());
    
    // Directories and file paths
    private static final String FACE_CASCADE_FILE = "data/haarcascade_frontalface_default.xml";
    private static final String FACE_DATA_DIR = "data/faces/";
    private static final String MODEL_DIR = "data/models/";
    private static final String MODEL_FILE = MODEL_DIR + "face_model.yml";
    
    // Constants for face detection and recognition
    private static final int FACE_WIDTH = 200;
    private static final int FACE_HEIGHT = 200;
    private static final int NUM_TRAINING_IMAGES = 10;
    private static final int CONFIDENCE_THRESHOLD = 70;
    
    // OpenCV components
    private CascadeClassifier faceDetector;
    private LBPHFaceRecognizer faceRecognizer;
    
    // Singleton instance
    private static FaceRecognitionService instance;
    
    /**
     * Get the singleton instance of the service
     * 
     * @return the FaceRecognitionService instance
     */
    public static synchronized FaceRecognitionService getInstance() {
        if (instance == null) {
            instance = new FaceRecognitionService();
        }
        return instance;
    }
    
    /**
     * Private constructor for the service
     */
    private FaceRecognitionService() {
        try {
            // Load the OpenCV native library
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            
            // Initialize face detector and recognizer
            faceDetector = new CascadeClassifier(FACE_CASCADE_FILE);
            if (faceDetector.empty()) {
                LOGGER.severe("Failed to load face cascade classifier");
                throw new RuntimeException("Failed to load face cascade classifier");
            }
            
            // Create face recognizer
            faceRecognizer = LBPHFaceRecognizer.create();
            
            // Load existing model if available
            File modelFile = new File(MODEL_FILE);
            if (modelFile.exists()) {
                faceRecognizer.read(MODEL_FILE);
                LOGGER.info("Loaded existing face recognition model");
            }
            
            // Create directories if they don't exist
            Files.createDirectories(Paths.get(FACE_DATA_DIR));
            Files.createDirectories(Paths.get(MODEL_DIR));
            
            LOGGER.info("FaceRecognitionService initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing FaceRecognitionService", e);
            throw new RuntimeException("Error initializing FaceRecognitionService", e);
        }
    }
    
    /**
     * Register face for a student
     * 
     * @param studentId the student ID
     * @return true if registration is successful
     */
    public boolean registerFace(int studentId) {
        VideoCapture capture = new VideoCapture(0);
        
        if (!capture.isOpened()) {
            LOGGER.severe("Failed to open camera");
            return false;
        }
        
        try {
            // Create directory for the student's face images
            String studentDir = FACE_DATA_DIR + studentId + "/";
            Files.createDirectories(Paths.get(studentDir));
            
            // Capture multiple images for training
            List<Mat> faceImages = new ArrayList<>();
            int imagesCollected = 0;
            
            Mat frame = new Mat();
            
            LOGGER.info("Starting face registration for student " + studentId);
            
            while (imagesCollected < NUM_TRAINING_IMAGES) {
                if (capture.read(frame)) {
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    
                    // Detect faces
                    MatOfRect faces = new MatOfRect();
                    faceDetector.detectMultiScale(grayFrame, faces);
                    
                    for (Rect rect : faces.toArray()) {
                        // Process only one face (the largest one)
                        Mat face = new Mat(grayFrame, rect);
                        Mat resizedFace = new Mat();
                        Imgproc.resize(face, resizedFace, new Size(FACE_WIDTH, FACE_HEIGHT));
                        
                        // Save the face image
                        String filename = studentDir + "face_" + imagesCollected + ".jpg";
                        Imgcodecs.imwrite(filename, resizedFace);
                        
                        faceImages.add(resizedFace.clone());
                        imagesCollected++;
                        
                        LOGGER.info("Captured face image " + imagesCollected + " for student " + studentId);
                        
                        // Add a delay before capturing the next image
                        Thread.sleep(500);
                        
                        break;  // Process only the first detected face
                    }
                }
            }
            
            // Train the recognizer with the collected face images
            if (faceImages.size() == NUM_TRAINING_IMAGES) {
                MatOfInt labels = new MatOfInt();
                List<Integer> labelsList = new ArrayList<>();
                for (int i = 0; i < NUM_TRAINING_IMAGES; i++) {
                    labelsList.add(studentId);
                }
                labels.fromList(labelsList);
                
                faceRecognizer.train(faceImages, labels);
                faceRecognizer.write(MODEL_FILE);
                
                LOGGER.info("Face recognition model updated for student " + studentId);
                return true;
            } else {
                LOGGER.warning("Failed to collect enough face images for student " + studentId);
                return false;
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during face registration", e);
            return false;
        } finally {
            capture.release();
        }
    }
    
    /**
     * Verify a student's face from camera input
     * 
     * @param studentId the student ID to verify
     * @return true if the face verification is successful
     */
    public boolean verifyFace(int studentId) {
        VideoCapture capture = new VideoCapture(0);
        
        if (!capture.isOpened()) {
            LOGGER.severe("Failed to open camera");
            return false;
        }
        
        try {
            Mat frame = new Mat();
            
            // Allow a few frames for the camera to adjust
            for (int i = 0; i < 10; i++) {
                capture.read(frame);
            }
            
            // Capture a face for verification
            if (capture.read(frame)) {
                Mat grayFrame = new Mat();
                Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                
                // Detect faces
                MatOfRect faces = new MatOfRect();
                faceDetector.detectMultiScale(grayFrame, faces);
                
                for (Rect rect : faces.toArray()) {
                    // Process only one face (the largest one)
                    Mat face = new Mat(grayFrame, rect);
                    Mat resizedFace = new Mat();
                    Imgproc.resize(face, resizedFace, new Size(FACE_WIDTH, FACE_HEIGHT));
                    
                    // Perform face recognition
                    int[] label = new int[1];
                    double[] confidence = new double[1];
                    faceRecognizer.predict(resizedFace, label, confidence);
                    
                    LOGGER.info("Face verification result for student " + studentId + 
                              ": detected label = " + label[0] + ", confidence = " + confidence[0]);
                    
                    // Check if the detected face matches the student ID
                    if (label[0] == studentId && confidence[0] < CONFIDENCE_THRESHOLD) {
                        LOGGER.info("Face verification successful for student " + studentId);
                        return true;
                    }
                    
                    break;  // Process only the first detected face
                }
            }
            
            LOGGER.warning("Face verification failed for student " + studentId);
            return false;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during face verification", e);
            return false;
        } finally {
            capture.release();
        }
    }
    
    /**
     * Check if a student's face is registered
     * 
     * @param studentId the student ID
     * @return true if the student's face is registered
     */
    public boolean isFaceRegistered(int studentId) {
        Path studentDir = Paths.get(FACE_DATA_DIR + studentId + "/");
        if (!Files.exists(studentDir)) {
            return false;
        }
        
        try {
            long fileCount = Files.list(studentDir)
                    .filter(file -> file.toString().endsWith(".jpg"))
                    .count();
            
            return fileCount >= NUM_TRAINING_IMAGES;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if face is registered", e);
            return false;
        }
    }
    
    /**
     * Delete registered face data for a student
     * 
     * @param studentId the student ID
     * @return true if deletion is successful
     */
    public boolean deleteFaceData(int studentId) {
        Path studentDir = Paths.get(FACE_DATA_DIR + studentId + "/");
        if (!Files.exists(studentDir)) {
            return true;  // Already deleted
        }
        
        try {
            // Delete all face images for the student
            Files.walk(studentDir)
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
            
            // Delete the student directory
            Files.delete(studentDir);
            
            LOGGER.info("Deleted face data for student " + studentId);
            
            // Retrain the model without this student's data
            // This would require retraining with all other student data
            // For simplicity, we'll just note that this should be done
            LOGGER.info("Face recognition model should be retrained");
            
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting face data", e);
            return false;
        }
    }
}