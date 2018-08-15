package com.example.android.emojify;
/*
 * Copyright (C) 2017 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

class Emojifier {
    private static final String LOG_TAG = Emojifier.class.getSimpleName();

    //.15, .5
    private static final float EMOJI_SCALE_FACTOR = .9f;
    private static final double SMILING_PROB_THRESHOLD = .18;
    private static final double EYE_OPEN_PROB_THRESHOLD = .5;
    /**
     * Method for detecting faces in a bitmap.
     * @param context The application context.
     * @param picture The picture in which to detect the faces.
     */
    static Bitmap detectFacesandOverlayEmoji (Context context, Bitmap picture) {
        Frame frame;
        // Create the face detector, disable tracking and enable classifications
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        // Build the frame
         frame = new Frame.Builder().setBitmap(picture).build();
        // Detect the faces
        SparseArray<Face> faces = detector.detect(frame);
        // Log the number of faces
        Log.d(LOG_TAG, "detectFaces: number of faces = " + faces.size());

        // Initialize result bitmap to original picture
        Bitmap resultBitmap = picture;
        // If there are no faces detected, show a Toast message
        if(faces.size() == 0){
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show();
        }
        else {
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);
                // Log the classification probabilities for each face.

                Bitmap emojiBitmap;
                switch (whichEmoji(face)) {
                    case SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.smile);
                        break;
                    case FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_frown);
                        break;
                    default:
                        emojiBitmap = null;
                        Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_SHORT).show();
                }



                // Add the emojiBitmap to the proper position in the original image

                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap, face);

            }
        }

        // Release the detector
        detector.release();
        return resultBitmap;
    }

    /**
     * Method for logging the classification probabilities.
     * @param face The face to get the classification probabilities.
     */

    private static Emoji whichEmoji(Face face){
        // Log all the probabilities
        Log.e(LOG_TAG, "whichEmoji: smilingProb = " + face.getIsSmilingProbability());
        Log.e(LOG_TAG, "whichEmoji: leftEyeOpenProb = "
                + face.getIsLeftEyeOpenProbability());
        Log.e(LOG_TAG, "whichEmoji: rightEyeOpenProb = "
                + face.getIsRightEyeOpenProbability());

        // Determine and log the appropriate emoji
        Emoji emoji;
        boolean smiling = face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD;
        Log.e("smile Tresh", "whichEmoji: is Smiling" + smiling);
        boolean leftEyeClosed = face.getIsLeftEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;
        Log.e("Left eye open Tresh", "whichEmoji: lefteye closed" + leftEyeClosed);
        boolean rightEyeClosed = face.getIsRightEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;
        Log.e("Ryt eye open Tresh", "whichEmoji: lefteye closed" + rightEyeClosed);
        if(smiling) {
            if (leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.LEFT_WINK;
            }  else if(rightEyeClosed && !leftEyeClosed){
                emoji = Emoji.RIGHT_WINK;
            } else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_SMILE;
            } else {
                emoji = Emoji.SMILE;
            }
        } else {
            if (leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.LEFT_WINK_FROWN;
            }  else if(rightEyeClosed && !leftEyeClosed){
                emoji = Emoji.RIGHT_WINK_FROWN;
            } else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_FROWN;
            } else {
                emoji = Emoji.FROWN;
            }
        }

        // Log the chosen Emoji
        Log.d(LOG_TAG, "whichEmoji: " + emoji.name());
        return emoji;
    }

    /**

     * Combines the original picture with the emoji bitmaps
     *
     * @param backgroundBitmap The original picture
     * @param emojiBitmap      The chosen emoji
     * @param face             The detected face
     * @return The final bitmap, including the emojis over the faces

     */

    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {
        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);
        // Determine the emoji position so it best lines up with the face

        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;

        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }

    // Enum for all possible Emojis
    private enum Emoji {
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN
    }

}