package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Activity responsible for providing a custom image cropping interface.
 * Forces a 1:1 circular (oval) crop ratio specifically designed for profile pictures.
 * Receives an input image URI, allows the user to adjust the crop region, and saves the
 * resulting bitmap to a temporary cache file with a unique timestamp before returning the new URI.

 * Written with the assistance of Gemini:
 * Prompt: "How can I add profile pictures onto an app in andriod studio, also also how can I make it live update?"
 */
public class CustomCropActivity extends AppCompatActivity {

    private CropImageView cropImageView;

    /**
     * Initializes the activity, sets the custom layout, and configures the CropImageView.
     * Extracts the image URI passed via Intent, enforces the circular crop constraints,
     * and handles the confirm and cancel button interactions. Upon confirmation, compresses
     * the cropped image and writes it to a local cache file with a unique timestamp to
     * prevent UI caching issues.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.crop_layout);

        cropImageView = findViewById(R.id.customCropImageView);
        Button btnCancel = findViewById(R.id.btnCancelCrop);
        Button btnConfirm = findViewById(R.id.btnConfirmCrop);

        // Force a 1:1 Circle crop
        cropImageView.setCropShape(CropImageView.CropShape.OVAL);
        cropImageView.setFixedAspectRatio(true);

        // Get the image passed from UserProfileActivity
        Uri sourceUri = getIntent().getParcelableExtra("imageUri");
        if (sourceUri != null) {
            cropImageView.setImageUriAsync(sourceUri);
        }

        // Cancel just closes the screen
        btnCancel.setOnClickListener(v -> finish());

        // Confirm grabs the bitmap and saves it using standard Android methods
        btnConfirm.setOnClickListener(v -> {
            Bitmap croppedBitmap = cropImageView.getCroppedImage();

            if (croppedBitmap != null) {
                try {
                    // Create a temporary file in the app's cache
                    File tempFile = new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream out = new FileOutputStream(tempFile);

                    // Compress and save the bitmap to the file
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();

                    // Convert the file back to a URI to send to Firebase
                    Uri destinationUri = Uri.fromFile(tempFile);

                    // Send it back to UserProfileActivity
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("croppedUri", destinationUri);
                    setResult(RESULT_OK, returnIntent);
                    finish();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}