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
    StorageReference storageReference;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    Uri fileUri;
    byte[] fileByteData;
    Bitmap fileBitmap;
    //nếu file là bitmap
    public UploadFileToFirebase(Context c,Bitmap fileBitmap){
        context=c;
        this.fileBitmap = fileBitmap;
        fileByteData =getByteDataFromBitmap(fileBitmap);
        storageReference = FirebaseStorage.getInstance().getReference("Images");
    }
    //nếu file là uri
    public UploadFileToFirebase(Context c,Uri fileUri){
        context=c;
        this.fileUri =fileUri;
        storageReference = FirebaseStorage.getInstance().getReference("Images");
    }
    //lấy dữ liệu ảnh uri để trả về đuôi ảnh là png hay jpg
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //nếu ảnh là bitmap thì trả về byte[]
    private byte[] getByteDataFromBitmap(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG,100,bytes);
        byte[] data = bytes.toByteArray();
        return data;
    }
    public Task<Uri> uploadImage() {
        final StorageReference fileReference;
        if(fileUri !=null) {//nếu ảnh lấy từ thư viện
            fileReference = storageReference.child(UUID.randomUUID() + "" + System.currentTimeMillis()
                    + "." + getFileExtension(fileUri));
            uploadTask = fileReference.putFile(fileUri);
        }
        else { //nếu ảnh vừa chụp
            fileReference = storageReference.child(UUID.randomUUID() + "" + System.currentTimeMillis());
            uploadTask = fileReference.putBytes(fileByteData);
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
