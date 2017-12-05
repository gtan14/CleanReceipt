package com.example.cleanreceipt;

import android.Manifest;
import android.app.Dialog;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.INPUT_METHOD_SERVICE;


public class UploadReceipt extends android.support.v4.app.Fragment implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    ImageView uploadImage;
    ImageView takePicture;
    ImageView uploadedImage;
    Spinner SpCategories;

    String mCurrentPhotoPath;
    EditText receiptNameEditText;
    EditText receiptDateEditText;
    EditText receiptPriceEditText;
    Spinner spinnerCategory;
    AutoCompleteTextView receiptLocationAutoComplete;
    TextInputLayout receiptNameTextInputLayout;
    TextInputLayout receiptDateTextInputLayout;
    TextInputLayout receiptPriceTextInputLayout;
    Uri photoURI;
    File photoFile;
    Button cancel;
    Button saveNewReceipt;
    Bitmap rotatedBitmap;
    ProgressBar progressBar;
    private static final int SELECT_PICTURE = 1;
    private static final int CAMERA_REQUEST = 2;
    Context ctx;
    boolean landscape;
    boolean cameraIntentOrientation;
    LoadImage loadImage;
    ConstraintLayout constraintLayout;
    ScrollView textViewScroll;
    Location lastLocation;
    List<String> defaultCategorySuggestions;
    List<String> updatedCategorySuggestions;

    /**
     * GeoDataClient wraps our service connection to Google Play services and provides access
     * to the Google Places API for Android.
     */
    protected GeoDataClient mGeoDataClient;

    private PlaceAutocompleteAdapter mAdapter;

    private long UPDATE_INTERVAL = 5 * 15000;
    private long FASTEST_INTERVAL = 2000;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;

    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    LatLngBounds latLngBounds;

    //Assigns activity to UploadReceipt context once Home is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context != null){
            ctx = context;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returns layout file
        View v= inflater.inflate(R.layout.activity_main, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {

        //view initialization
        takePicture = (ImageView) v.findViewById(R.id.takePicture);
        uploadImage = (ImageView) v.findViewById(R.id.uploadImage);
        uploadedImage = (ImageView) v.findViewById(R.id.uploadedImage);
        cancel = (Button) v.findViewById(R.id.cancelNewReceipt);
        saveNewReceipt = (Button) v.findViewById(R.id.saveNewReceipt);
        progressBar = (ProgressBar) v.findViewById(R.id.imageProgressBar);

        receiptNameEditText = (EditText) v.findViewById(R.id.receiptNameEditText);
        receiptDateEditText = (EditText) v.findViewById(R.id.receiptDateEditText);
        receiptPriceEditText = (EditText) v.findViewById(R.id.receiptPriceEditText);
        spinnerCategory = (Spinner) v.findViewById(R.id.receiptCategorySpinner);
        receiptLocationAutoComplete = (AutoCompleteTextView) v.findViewById(R.id.receiptLocationAutoComplete);

        receiptDateTextInputLayout = (TextInputLayout) v.findViewById(R.id.receiptDateTextInputLayout);
        receiptNameTextInputLayout = (TextInputLayout) v.findViewById(R.id.receiptNameTextInputLayout);
        receiptPriceTextInputLayout = (TextInputLayout) v.findViewById(R.id.receiptPriceTextInputLayout);

        constraintLayout = (ConstraintLayout) v.findViewById(R.id.receiptConstraintLayout);

        isGooglePlayServicesAvailable();
        if(!isLocationEnabled()){
            showAlert();
        }

        googleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(UploadReceipt.this)
                .addOnConnectionFailedListener(UploadReceipt.this)
                .addApi(LocationServices.API)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onStop(){
        if(googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onStop();
    }


    //suggestions will appear within a certain radius mDistanceInMeters
    public static LatLngBounds setBounds(Location location, int mDistanceInMeters ) {
        double latRadian = Math.toRadians(location.getLatitude());
        double degLatKm = 110.574235;
        double degLongKm = 110.572833 * Math.cos(latRadian);
        double deltaLat = mDistanceInMeters / 1000.0 / degLatKm;
        double deltaLong = mDistanceInMeters / 1000.0 / degLongKm;

        double minLat = location.getLatitude() - deltaLat;
        double minLong = location.getLongitude() - deltaLong;
        double maxLat = location.getLatitude() + deltaLat;
        double maxLong = location.getLongitude() + deltaLong;


        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.

        return new LatLngBounds(new LatLng(minLat, minLong), new LatLng(maxLat, maxLong));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latLngBounds = setBounds(location, 160934);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, UploadReceipt.this);

            } else {
                //If everything went fine lets get latitude and longitude
                latLngBounds = setBounds(lastLocation, 160934);

            }
        }
        catch (SecurityException e){

        }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(ctx, "onConnectionFailed: \n" + connectionResult.toString(),
                Toast.LENGTH_LONG).show();
        Log.d("DDD", connectionResult.toString());
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);


        defaultCategorySuggestions = Arrays.asList("Groceries", "Rent", "Phone", "Car insurance", "Gas", "Utilities", "Books"
                , "Free spending", "Emergency", "Category");



        setCategoryAdapter(defaultCategorySuggestions);

        googleApiClient.connect();

        mGeoDataClient = Places.getGeoDataClient(ctx, null);

        receiptLocationAutoComplete.setOnItemClickListener(mAutocompleteClickListener);

        mAdapter = new PlaceAutocompleteAdapter(ctx, mGeoDataClient, latLngBounds, null);
        receiptLocationAutoComplete.setAdapter(mAdapter);

        getActivity().setTitle("Upload Receipt");


        final SQLiteHelper sqLiteHelper = new SQLiteHelper(ctx);

        removeErrorShowHint();
        uploadedImage.setVisibility(View.INVISIBLE);


        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                String year = String.format("%s", i);
                String month = String.format("%s", i1 + 1);
                String day = String.format("%s", i2);

                String formattedDate = month + "/" + day + "/" + year;
                receiptDateEditText.setText(formattedDate);
            }
        };

        receiptDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)ctx.getSystemService(INPUT_METHOD_SERVICE);
                    if(imm != null && getActivity().getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                DatePickerDialog dialog = new DatePickerDialog(ctx, onDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        receiptDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SimpleDateFormat dayMonthYearFormat = new SimpleDateFormat("MM/d/yy");
                SimpleDateFormat monthAndYear = new SimpleDateFormat("MMMMyyyy");
                try {
                    Date date = dayMonthYearFormat.parse(editable.toString());
                    String possibleTable = monthAndYear.format(date) + "Month";

                    ArrayList<String> tableNames = sqLiteHelper.getTableNames();

                    setCategoryAdapter(defaultCategorySuggestions);
                    if(tableNames.size() > 1) {
                        for (int j = 0; j < tableNames.size(); j++) {
                            if (tableNames.get(j).equals(possibleTable)) {
                                ArrayList<BudgetModel> budgetModelList = sqLiteHelper.getBudgetRecords(tableNames.get(j));
                                List<String> newCategories = new ArrayList<>();
                                if(budgetModelList.size() > 0) {
                                    BudgetModel budgetModel;
                                    for (int i = 0; i < budgetModelList.size(); i++) {
                                        budgetModel = budgetModelList.get(i);
                                        String categoryString = budgetModel.getCategory();
                                        newCategories.add(categoryString);
                                    }
                                    newCategories.add("Category");
                                    Log.d("text", "text");
                                    setCategoryAdapter(newCategories);
                                }
                            }

                        }

                    }
                }
                catch(ParseException e){

                }
            }
        });


        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
            }
        });

        uploadedImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                registerForContextMenu(view);
                return false;
            }
        });


        uploadedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bm = ((BitmapDrawable)uploadedImage.getDrawable()).getBitmap();
                final Dialog nagDialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                nagDialog.setCancelable(false);
                nagDialog.setContentView(R.layout.fullscreen_image);
                ImageView fullscreenImagePortrait = (ImageView) nagDialog.findViewById(R.id.fullscreenImagePortrait);
                ImageView fullscreenImageLandscape = (ImageView) nagDialog.findViewById(R.id.fullscreenImageLandscape);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;


                if(!landscape) {
                    fullscreenImagePortrait.setImageBitmap(bm);
                    //bm.recycle();

                    fullscreenImagePortrait.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {

                            nagDialog.dismiss();
                        }
                    });
                }
                else{
                    Bitmap scaledBitmap = createScaledBitmap(bm, width, height / 2, ScalingLogic.FIT);
                    fullscreenImageLandscape.setImageBitmap(scaledBitmap);
                    //bm.recycle();

                    fullscreenImageLandscape.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {

                            nagDialog.dismiss();
                        }
                    });
                }
                nagDialog.show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(constraintLayout.getWindowToken(), 0);
                }

                if(loadImage != null) {
                    if (loadImage.getStatus() == AsyncTask.Status.RUNNING) {
                        loadImage.cancel(true);
                    }
                }
            }
        });

        saveNewReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tableNum = 1;
                String tableName = "receipt" + String.format("%s", tableNum);
                int children = constraintLayout.getChildCount();
                boolean save = true;
                SimpleDateFormat dayMonthYearFormat = new SimpleDateFormat("MM/d/yy");
                SimpleDateFormat monthAndYear = new SimpleDateFormat("MMMMyyyy");

                try {
                    Date date = dayMonthYearFormat.parse(receiptDateEditText.toString());
                    String possibleTable = monthAndYear.format(date) + "Month";

                    ArrayList<String> tableNames = sqLiteHelper.getTableNames();

                    if (tableNames.size() > 1) {
                        for (int j = 0; j < tableNames.size(); j++) {
                            if (tableNames.get(j).equals(possibleTable)) {
                                ArrayList<BudgetModel> budgetModelList = sqLiteHelper.getBudgetRecords(tableNames.get(j));
                                if (budgetModelList.size() > 0) {
                                    BudgetModel budgetModel;
                                    for (int i = 0; i < budgetModelList.size(); i++) {
                                        budgetModel = budgetModelList.get(i);
                                        String categoryString = budgetModel.getCategory();
                                        if(!categoryString.equals(spinnerCategory.getSelectedItem().toString())){
                                            save = false;
                                            Toast.makeText(ctx, "Category does not exist for budget", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch(ParseException e){

                    }
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(constraintLayout.getWindowToken(), 0);
                }

                for(int i = 0; i < children; i++){
                    View v = constraintLayout.getChildAt(i);
                    if(v instanceof TextInputLayout){
                        EditText editText = ((TextInputLayout) v).getEditText();
                        if(editText != null) {
                            editText.clearFocus();
                        }
                    }
                }


                if(receiptPriceEditText.getText().length() == 0){
                    receiptPriceTextInputLayout.setErrorEnabled(true);
                    receiptPriceTextInputLayout.setHintEnabled(false);
                    receiptPriceTextInputLayout.setError("Input price");
                    save = false;
                }

                if(uploadedImage.getDrawable() == null){
                    Toast.makeText(ctx, "Upload an image", Toast.LENGTH_LONG).show();
                    save = false;
                }

                if(spinnerCategory.getSelectedItem().toString().equals("Category")){
                    Toast.makeText(ctx, "Select a category", Toast.LENGTH_LONG).show();
                    save = false;
                }

                if(receiptDateEditText.getText().length() == 0){
                    receiptDateTextInputLayout.setErrorEnabled(true);
                    receiptDateTextInputLayout.setHintEnabled(false);
                    receiptDateTextInputLayout.setError("Input date");
                    save = false;
                }

                if(receiptNameEditText.getText().length() == 0){
                    receiptNameTextInputLayout.setErrorEnabled(true);
                    receiptNameTextInputLayout.setHintEnabled(false);
                    receiptNameTextInputLayout.setError("Input name");
                    save = false;
                }

                if(loadImage != null) {
                    if (loadImage.getStatus() == AsyncTask.Status.RUNNING) {
                        Toast toast = Toast.makeText(getActivity(), "Save unsuccessful", Toast.LENGTH_SHORT);
                        toast.show();
                        save = false;
                    }
                }

                if(save) {
                    while (sqLiteHelper.tableExists(tableName)) {
                        ++tableNum;
                        tableName = "receipt" + String.format("%s", tableNum);
                    }

                    BitmapDrawable drawable = (BitmapDrawable) uploadedImage.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    byte[] byteArray = getBytes(bitmap);
                    sqLiteHelper.createReceiptTable(tableName);
                    ReceiptModel receiptModel = new ReceiptModel();
                    receiptModel.setDate(receiptDateEditText.getText().toString());
                    receiptModel.setName(receiptNameEditText.getText().toString());
                    receiptModel.setPrice(receiptPriceEditText.getText().toString());
                    receiptModel.setCategory(spinnerCategory.getSelectedItem().toString());
                    receiptModel.setLocation(receiptLocationAutoComplete.getText().toString());
                    receiptModel.setImage(byteArray);
                    Log.d("byte", String.format("%s", byteArray.length));
                    sqLiteHelper.insertRecordReceipt(receiptModel, tableName, byteArray);


                    try {
                        Date date = dayMonthYearFormat.parse(receiptDateEditText.getText().toString());
                        String possibleTable = monthAndYear.format(date) + "Month";
                        sqLiteHelper.updateColumn(receiptPriceEditText.getText().toString(), possibleTable, spinnerCategory.getSelectedItem().toString());
                    }
                    catch (ParseException e){

                    }

                    android.support.v4.app.Fragment receiptLookUp = new ReceiptLookUp();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, receiptLookUp, "FragmentHome");
                    ft.commit();
                }
            }
        });




    }

    //Allows user to pick the image from a source (gallery)
    private void getImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);
    }


    //Gets the image from the source and creates a bitmap
    //Assigns the bitmap to an image view, and hides the upload and take picture button
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if user selects a picture from gallery
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && null != data) {

            cameraIntentOrientation = false;

            //creates and starts an async task that will get the image selected from the Uri and displays it to an imageview
            //displays a progress bar while image is loading
            loadImage = new LoadImage(this);
            loadImage.execute(data.getData());
            //receiptLocationAutoComplete.showDropDown();
            //receiptLocationAutoComplete.requestFocus();
        }

        //if user takes a picture
        else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){

            cameraIntentOrientation = true;

            loadImage = new LoadImage(this);
            loadImage.execute(photoURI);
        }

    }


    //Method to show the option to delete image
    //Calls when image view is long pressed
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    //Calls deleteImage() when delete is pressed
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.deletePicture:
                deleteImage();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //Makes the image view null and visibility gone
    //Option to upload image or take picture becomes visible again
    public void deleteImage(){
        uploadedImage.setImageDrawable(null);
        uploadedImage.setVisibility(View.INVISIBLE);
        uploadImage.setVisibility(View.VISIBLE);
        takePicture.setVisibility(View.VISIBLE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("IOException", "IOException");
                // Error occurred while creating the File
                //...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }


    //method for getting the orientation of an image
    //if returns 90, 180, or 270, rotate image accordingly
    public float getExifOrientation(Uri photoUri) {
        float photoRotation = 0;
        boolean hasRotation = false;
        String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
        try {
            Cursor cursor = getActivity().getContentResolver().query(photoUri, projection, null, null, null);
            if(cursor != null) {
                if (cursor.moveToFirst()) {
                    photoRotation = cursor.getInt(0);
                    hasRotation = true;
                }
                cursor.close();
            }
        } catch (Exception e) {}

        if (!hasRotation) {
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(photoUri.getPath());
            }
            catch (IOException e){

            }
            if(exif != null) {
                int exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                switch (exifRotation) {
                    case ExifInterface.ORIENTATION_ROTATE_90: {
                        photoRotation = 90.0f;
                        break;
                    }
                    case ExifInterface.ORIENTATION_ROTATE_180: {
                        photoRotation = 180.0f;
                        break;
                    }
                    case ExifInterface.ORIENTATION_ROTATE_270: {
                        photoRotation = 270.0f;
                        break;
                    }
                }
            }
        }
        return photoRotation;
    }

    //method for rotating image for camera intent if necessary
    public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }


            Log.v(TAG, "Exif orientation: " + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }



    /**
     * Taken from Ruslan Yanchyshyn on Stack Overflow
     * @param bmp input bitmap
     * @param contrast 0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    public Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
    {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight, boolean recycleOriginal) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        // Determine scale to change size
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // Create Matrix for maniuplating size
        Matrix matrix = new Matrix();
        // Set the Resize Scale for the Matrix
        matrix.postScale(scaleWidth, scaleHeight);

        //Create a new Bitmap from original using matrix and new width/height
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        //Remove memory leaks if told to recycle, warning, if using original else where do not recycle it here
        if(recycleOriginal) {
            bm.recycle();

        }

        //Return the scaled new bitmap
        return resizedBitmap;

    }

    public Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

    public Bitmap decodeSampledBitmapFromFile(String pathName,
                                              int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = ctx.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                ;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                res = cursor.getString(column_index);
            }
            cursor.close();
        }
        return res;
    }


    /**
     * Utility function for creating a scaled version of an existing bitmap
     *
     * @param unscaledBitmap Bitmap to scale
     * @param dstWidth Wanted width of destination bitmap
     * @param dstHeight Wanted height of destination bitmap
     * @param scalingLogic Logic to use to avoid image stretching
     * @return New scaled bitmap object
     */
    public Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight,
                                            ScalingLogic scalingLogic) {
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight, scalingLogic);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight, scalingLogic);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    /**
     * ScalingLogic defines how scaling should be carried out if source and
     * destination image has different aspect ratio.
     *
     * CROP: Scales the image the minimum amount while making sure that at least
     * one of the two dimensions fit inside the requested destination area.
     * Parts of the source image will be cropped to realize this.
     *
     * FIT: Scales the image the minimum amount while making sure both
     * dimensions fit inside the requested destination area. The resulting
     * destination dimensions might be adjusted to a smaller size than
     * requested.
     */
    public enum ScalingLogic {
        CROP, FIT
    }

    /**
     * Calculates source rectangle for scaling bitmap
     *
     * @param srcWidth Width of source image
     * @param srcHeight Height of source image
     * @param dstWidth Width of destination area
     * @param dstHeight Height of destination area
     * @param scalingLogic Logic to use to avoid image stretching
     * @return Optimal source rectangle
     */
    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.CROP) {
            final float srcAspect = (float)srcWidth / (float)srcHeight;
            final float dstAspect = (float)dstWidth / (float)dstHeight;

            if (srcAspect > dstAspect) {
                final int srcRectWidth = (int)(srcHeight * dstAspect);
                final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
            } else {
                final int srcRectHeight = (int)(srcWidth / dstAspect);
                final int scrRectTop = (int)(srcHeight - srcRectHeight) / 2;
                return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
            }
        } else {
            return new Rect(0, 0, srcWidth, srcHeight);
        }
    }

    /**
     * Calculates destination rectangle for scaling bitmap
     *
     * @param srcWidth Width of source image
     * @param srcHeight Height of source image
     * @param dstWidth Width of destination area
     * @param dstHeight Height of destination area
     * @param scalingLogic Logic to use to avoid image stretching
     * @return Optimal destination rectangle
     */
    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float)srcWidth / (float)srcHeight;
            final float dstAspect = (float)dstWidth / (float)dstHeight;

            if (srcAspect > dstAspect) {
                return new Rect(0, 0, dstWidth, (int)(dstWidth / srcAspect));
            } else {
                return new Rect(0, 0, (int)(dstHeight * srcAspect), dstHeight);
            }
        } else {
            return new Rect(0, 0, dstWidth, dstHeight);
        }
    }

    public void removeErrorShowHint(){

        receiptDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                receiptDateTextInputLayout.setErrorEnabled(false);
                receiptDateTextInputLayout.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        receiptDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    receiptDateTextInputLayout.setHintEnabled(true);
                }
            }
        });

        receiptNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                receiptNameTextInputLayout.setErrorEnabled(false);
                receiptNameTextInputLayout.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        receiptNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    receiptNameTextInputLayout.setHintEnabled(true);
                }
            }
        });

        receiptPriceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                receiptPriceTextInputLayout.setErrorEnabled(false);
                receiptPriceTextInputLayout.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        receiptPriceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    receiptPriceTextInputLayout.setHintEnabled(true);
                }
            }
        });


    }

    //exercise adapter for the exercise EditText
    //displays the options of exercises
    private ArrayAdapter<String> getCategoryAdapter(Context context){
        String[] exercise = new String[]{};
        if(context != null) {
            exercise = context.getResources().getStringArray(R.array.category);
        }
        return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, exercise);

    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data Client
     * to retrieve more details about the place.
     *
     * @see GeoDataClient#getPlaceById(String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data Client to retrieve a Place object with
             additional details about the place.
              */
            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

        }
    };

    /**
     * Callback for results from a Places Geo Data Client query that shows the first place result in
     * the details view on screen.
     */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                Log.d("placeAddress", place.getAddress().toString());
                Log.d("place", place.getName().toString());
                /*
                // Display the third party attributions if set.
                final CharSequence thirdPartyAttribution = places.getAttributions();
                if (thirdPartyAttribution == null) {
                    mPlaceDetailsAttribution.setVisibility(View.GONE);
                } else {
                    mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
                    mPlaceDetailsAttribution.setText(
                            Html.fromHtml(thirdPartyAttribution.toString()));
                }*/

                Log.i(TAG, "Place details received: " + place.getAddress());

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
                return;
            }
        }
    };

    /*
    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }
}*/

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGooglePlayServicesAvailable() {
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ctx);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.d(TAG, "This device is not supported.");
                //finish();
            }
            return false;
        }
        Log.d(TAG, "This device is supported.");
        return true;
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private void setCategoryAdapter(List<String> list){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, list){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setSelection(adapter.getCount());
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        try {
            stream.close();
        }
        catch (IOException e){

        }
        return stream.toByteArray();
    }


}
