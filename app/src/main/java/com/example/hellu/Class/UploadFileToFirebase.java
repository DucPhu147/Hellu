package com.example.hellu.Class;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class UploadFileToFirebase {
    Context context;
    boolean isImageFromGallery;
    StorageReference storageReference;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    Uri imageUri;
    byte[] imageData;
    Bitmap imageBitmap;
    public UploadFileToFirebase(Context c,boolean isUserProfileImage,Bitmap imageBitmap){
        context=c;
        this.imageBitmap=imageBitmap;
        this.isImageFromGallery=false;
        imageData=getByteDataFromBitmap(imageBitmap);
        if(!isUserProfileImage)
            storageReference = FirebaseStorage.getInstance().getReference("MessageImages");
        else
            storageReference = FirebaseStorage.getInstance().getReference("UserProfileImages");
    }
    public UploadFileToFirebase(Context c,boolean isUserProfileImage,Uri imageUri){
        context=c;
        this.imageUri=imageUri;
        this.isImageFromGallery=true;
        if(!isUserProfileImage)
            storageReference = FirebaseStorage.getInstance().getReference("MessageImages");
        else
            storageReference = FirebaseStorage.getInstance().getReference("UserProfileImages");
    }
    //trả về đuôi png hay jpg
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private byte[] getByteDataFromBitmap(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        byte[] data = bytes.toByteArray();
        return data;
    }
    public Task<Uri> uploadImage() {
        final StorageReference fileReference;
        if(isImageFromGallery) {//nếu ảnh lấy từ thư viện
            fileReference = storageReference.child(UUID.randomUUID() + "" + System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
        }
        else { //nếu ảnh vừa chụp
            fileReference = storageReference.child(UUID.randomUUID() + "" + System.currentTimeMillis()+".jpeg");
            uploadTask = fileReference.putBytes(imageData);
        }
        return uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        });
    }
}
