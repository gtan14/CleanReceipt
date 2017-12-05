package com.example.cleanreceipt;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Gerald on 11/5/2017.
 */

public class LoadImage extends AsyncTask<Uri, Void, ReceiptScanResults> {

    private UploadReceipt UploadReceipt;

    public LoadImage(UploadReceipt UploadReceipt){
        this.UploadReceipt = UploadReceipt;
    }

    @Override
    protected void onPreExecute()
    {
        //show loading dialog and remove takePicture and uploadImage from view
        UploadReceipt.progressBar.setVisibility(View.VISIBLE);
        UploadReceipt.progressBar.bringToFront();
        UploadReceipt.takePicture.setVisibility(View.GONE);
        UploadReceipt.uploadImage.setVisibility(View.GONE);

        //do initialization of required objects objects here
    };

    @Override
    protected ReceiptScanResults doInBackground(Uri... params)
    {
        ReceiptScanResults receiptScanResults = new ReceiptScanResults();
        //gets the bitmap from the Uri and rotates if necessary
        Bitmap bitmap = null;
        Bitmap resizedProcessedBitmap = null;
        String filePath = "";
        if(params != null){
            //try {
            //gets the image that is chosen and assigns it to a bitmap
            //bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), params[0]);

            //decodes the bitmap from the Uri with dimensions 700x700
            //String filePath = getRealPathFromURI(params[0]);
            //bitmap = decodeSampledBitmapFromFile(filePath, 700, 700);

            //creates new matrix and rotates if necessary
            Matrix matrix = new Matrix();

            //if picture is taken from camera
            if(UploadReceipt.cameraIntentOrientation){
                //String filePath = getRealPathFromURI(params[0]);
                bitmap = UploadReceipt.decodeSampledBitmapFromFile(UploadReceipt.mCurrentPhotoPath, 700, 700);
                matrix.postRotate(UploadReceipt.getCameraPhotoOrientation(UploadReceipt.ctx, params[0], UploadReceipt.mCurrentPhotoPath));
                if(UploadReceipt.getCameraPhotoOrientation(UploadReceipt.ctx, params[0], UploadReceipt.mCurrentPhotoPath) == 0){
                    UploadReceipt.landscape = true;
                }
                else{
                    UploadReceipt.landscape = false;
                }
            }

            else {
                filePath = UploadReceipt.getRealPathFromURI(params[0]);
                //bitmap = UploadReceipt.decodeSampledBitmapFromFile(filePath, 700, 700);
                try {
                    bitmap = UploadReceipt.decodeBitmapUri(UploadReceipt.ctx, params[0]);
                }
                catch (FileNotFoundException e){

                }

                matrix.postRotate(UploadReceipt.getExifOrientation(params[0]));

                //declares landscape true if getExifOrientation returns 0
                if (UploadReceipt.getExifOrientation(params[0]) == 0) {
                    UploadReceipt.landscape = true;
                } else {
                    UploadReceipt.landscape = false;
                }
            }

            //Log.d("exif", String.format("%s", getExifOrientation(params[0])));
            //Log.d("dataUri", String.format("%s", params[0]));

            //new rotated bitmap
            if(bitmap != null) {
                UploadReceipt.rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                receiptScanResults.setBitmap(UploadReceipt.rotatedBitmap);
            }

            //bitmap.recycle();

            //}
            //catch (IOException e){

            //}

        }

        //scan the bitmap for text using textRecognizer from Google Mobile Vision API
        if(UploadReceipt.rotatedBitmap != null) {


            TextRecognizer textRecognizer = new TextRecognizer.Builder(UploadReceipt.ctx).build();

            if(!textRecognizer.isOperational()) {
                // Note: The first time that an app using a Vision API is installed on a
                // device, GMS will download a native libraries to the device in order to do detection.
                // Usually this completes before the app is run for the first time.  But if that
                // download has not yet completed, then the above call will not detect any text,
                // barcodes, or faces.
                // isOperational() can be used to check if the required native libraries are currently
                // available.  The detectors will automatically become operational once the library
                // downloads complete on device.
                Log.d("test", "Detector dependencies are not yet available.");

                // Check for low storage.  If there is low storage, the native library will not be
                // downloaded, so detection will not become operational.
                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = UploadReceipt.ctx.registerReceiver(null, lowstorageFilter) != null;

                if (hasLowStorage) {
                    Toast.makeText(UploadReceipt.getActivity(),"Low Storage", Toast.LENGTH_LONG).show();
                    Log.d("test", "Low Storage");
                }
            }


            //uses image processing on the rotatedBitmap in order to achieve better results for ocr
            //Bitmap processedBitmap = rotatedBitmap;
            int width = UploadReceipt.rotatedBitmap.getWidth() * 2;
            int height = UploadReceipt.rotatedBitmap.getHeight() * 2;


            //image processing
            //Bitmap resizedBitmap = UploadReceipt.resize(bitmap, width, height);
            try{
                FileOutputStream out = new FileOutputStream(filePath);
                UploadReceipt.rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            }
            catch (Exception e){

            }
            resizedProcessedBitmap = UploadReceipt.getResizedBitmap(UploadReceipt.rotatedBitmap, width, height, false);
            resizedProcessedBitmap = UploadReceipt.changeBitmapContrastBrightness(resizedProcessedBitmap, 3, -100);
            resizedProcessedBitmap = UploadReceipt.toGrayscale(resizedProcessedBitmap);



            Frame imageFrame = new Frame.Builder()
                    .setBitmap(resizedProcessedBitmap)
                    .build();

            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            formatter.setLenient(false);
            Date date = null;
            String blocks = "";
            String lines = "";
            String words = "";
            String monthInWords = "";
            String day = "";
            String year = "";
            String addressString = "";
            double pricePaid = 0.00;
            List<Address> addressList = new ArrayList<>();
            HashMap hashMap = createHashMapForDate();
            Set set = hashMap.entrySet();
            Geocoder geocoder = new Geocoder(UploadReceipt.ctx);
            int zipCodeIndex = 0;

            for (int index = 0; index < textBlocks.size(); index++) {
                //extract scanned text blocks here
                TextBlock tBlock = textBlocks.valueAt(index);
                blocks = blocks + tBlock.getValue() + "\n" + "\n";
                for (Text line : tBlock.getComponents()) {
                    //extract scanned text lines here
                    lines = lines + line.getValue() + "\n";

                    String price = line.getValue().replace(",", ".");

                    if(zipCodeIndex == 1){
                        int possibleStateAbbr = 0;
                        for(int i = 0; i <line.getValue().length(); i++){
                            char ch = line.getValue().charAt(i);
                            if(Character.isUpperCase(ch)){
                                possibleStateAbbr++;
                            }
                        }
                        if(possibleStateAbbr >= 2) {
                            addressString += line.getValue();
                            zipCodeIndex = 0;
                        }
                    }
                    //the total price paid will always have a decimal
                    //if string does not have a decimal, it is not a price
                    if(price.contains(".")) {
                        try {
                            double possiblePricePaid = Double.parseDouble(price);
                            if (possiblePricePaid > pricePaid) {
                                pricePaid = possiblePricePaid;
                                Log.d("price", String.format("%s", pricePaid));
                            }
                        } catch (NumberFormatException e) {

                        }
                    }

                    //on most receipts, the date will be listed in a mm/dd/yyyy format
                    else if(line.getValue().contains("/")){
                        try {
                            date = formatter.parse(line.getValue());
                        } catch (ParseException e) {
                            //If input date is in different format or invalid.
                        }
                    }

                    Iterator iterator = set.iterator();
                    while(iterator.hasNext()){
                        Map.Entry me = (Map.Entry) iterator.next();
                        if(line.getValue().contains(me.getKey().toString())){
                            monthInWords = me.getValue().toString();

                            int indexOfMonth = line.getValue().indexOf(me.getKey().toString());
                            day = line.getValue().substring(indexOfMonth + 3, indexOfMonth + 5);

                            year = line.getValue().substring(indexOfMonth + 7, indexOfMonth + 9);
                        }
                    }

                    if(line.getValue().contains("Lane") || line.getValue().contains("Street") || line.getValue().contains("NW") || line.getValue().contains("Suite")
                            || line.getValue().contains("Road") || line.getValue().contains("Drive") || line.getValue().contains("Parkway") || line.getValue().contains("Ridge")){
                        addressString = line.getValue();
                        zipCodeIndex++;
                    }


                    //else if(line.getValue().)
                    for (Text element : line.getComponents()) {
                        //extract scanned text words here
                        words = words + element.getValue() + ", ";


                    }
                }
            }

            if(!addressString.isEmpty()){
                try {
                    addressList = geocoder.getFromLocation(UploadReceipt.lastLocation.getLatitude(), UploadReceipt.lastLocation.getLongitude(), 1);
                    String[] addressFromLocationArray = addressList.get(0).getAddressLine(0).split(",");
                    String addressFromLocation = addressFromLocationArray[0];

                    SimilarityStrategy strategy = new LevenshteinDistanceStrategy();
                    StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
                    double score = service.score(addressString, addressFromLocation);
                    Log.d("score", String.format("%s", score));
                    if(score > .75) {
                        receiptScanResults.setLocation(addressFromLocation);
                    }

                    else{
                        receiptScanResults.setLocation(addressString);
                    }
                }
                catch (IOException e){

                }
            }

            if(date != null){
                String month = android.text.format.DateFormat.format("MM", date).toString();
                String d = android.text.format.DateFormat.format("dd", date).toString();
                String y = android.text.format.DateFormat.format("yyyy", date).toString();
                String formattedDate = month + "/" + d + "/" + y;
                receiptScanResults.setDate(formattedDate);
            }

            else if(!monthInWords.isEmpty() && !day.isEmpty() && !year.isEmpty()){
                String formattedDate = monthInWords + "/" + day + "/" + year;
                receiptScanResults.setDate(formattedDate);
            }

            else{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                String dateFormatted = simpleDateFormat.format(new Date());
                receiptScanResults.setDate(dateFormatted);
            }

            if(pricePaid != 0.0) {
                receiptScanResults.setPrice(String.format("%s", pricePaid));
            }

            if (textBlocks.size() == 0) {
                Log.d("ocr", "Scan Failed: Found nothing to scan");
            } else {

                Log.d("ocr", "Blocks: " + "\n");
                Log.d("ocr", blocks + "\n");
                Log.d("ocr", "---------" + "\n");
                Log.d("ocr", "Lines: " + "\n");
                Log.d("ocr", lines + "\n");
                Log.d("ocr", "---------" + "\n");
                Log.d("ocr", "Words: " + "\n");
                Log.d("ocr", words + "\n");
                Log.d("ocr", "---------" + "\n");
            }
            //resizedProcessedBitmap.recycle();
        }

        return receiptScanResults;
    }
    @Override
    protected void onPostExecute(ReceiptScanResults receiptScanResults) {
        super.onPostExecute(receiptScanResults);

        Bitmap bitmap = receiptScanResults.getBitmap();
        String price = receiptScanResults.getPrice();
        String date = receiptScanResults.getDate();
        String location = receiptScanResults.getLocation();

        //removes progress bar from view and displays bitmap to imageView
        UploadReceipt.progressBar.setVisibility(View.GONE);
        UploadReceipt.uploadedImage.setVisibility(View.VISIBLE);
        UploadReceipt.uploadedImage.setImageBitmap(bitmap);
        UploadReceipt.receiptLocationAutoComplete.setText(location);
        UploadReceipt.receiptLocationAutoComplete.requestFocus();
        InputMethodManager imm = (InputMethodManager) UploadReceipt.ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        //UploadReceipt.receiptLocationAutoComplete.showDropDown();
        UploadReceipt.receiptDateEditText.setText(date);
        UploadReceipt.receiptPriceEditText.setText(price);
    }

    private HashMap createHashMapForDate(){
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("Jan", "1");
        hashMap.put("Feb", "2");
        hashMap.put("Mar", "3");
        hashMap.put("Apr", "4");
        hashMap.put("May", "5");
        hashMap.put("Jun", "6");
        hashMap.put("Jul", "7");
        hashMap.put("Aug", "8");
        hashMap.put("Sep", "9");
        hashMap.put("Oct", "10");
        hashMap.put("Nov", "11");
        hashMap.put("Dec", "12");
        return hashMap;
    }

}
