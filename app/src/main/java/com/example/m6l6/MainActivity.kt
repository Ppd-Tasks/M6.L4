package com.example.m6l6

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.*
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    var isPersistent:Boolean = false
    var isInternal = true
    var readPermissionGranted = false
    var writePermissionGranted = false
    var locationPermissionGranted = false
    var cameraPermissionGranted = false
    var APP_PERMISSION_CODE = 1001
    lateinit var recyclerView:RecyclerView
    lateinit var adapter: PhotoAdapter
    lateinit var adapter2: PhotoAdapter2
    private var photos = mutableListOf<Uri>()
    private var images = mutableListOf<Bitmap>()

    lateinit var btn_giveExternalPermission:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    fun initViews(){
        val btn_saveInternal = findViewById<Button>(R.id.btn_saveInternal)
        val btn_readInternal = findViewById<Button>(R.id.btn_readInternal)
        val btn_deleteInternal = findViewById<Button>(R.id.btn_deleteInternal)
        val btn_saveExternal = findViewById<Button>(R.id.btn_saveExternal)
        val btn_readExternal = findViewById<Button>(R.id.btn_readExternal)
        val btn_deleteExternal = findViewById<Button>(R.id.btn_deleteExternal)
        val btn_takePhoto = findViewById<Button>(R.id.btn_takePhoto)

        btn_giveExternalPermission = findViewById(R.id.btn_giveExternalPermission)

        //internal
        btn_saveInternal.setOnClickListener {
            saveInternalFile("PDP Academy")
        }
        btn_readInternal.setOnClickListener {
            readInternalFile()
        }
        btn_deleteInternal.setOnClickListener {
            deleteInternalFile()
        }

        //external
        btn_saveExternal.setOnClickListener {
            saveExternalFile("PDP Academy Android B-13")
        }
        btn_readExternal.setOnClickListener {
            readExternalFile()
        }
        btn_deleteExternal.setOnClickListener {
            deleteExternalFile()
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setLayoutManager(GridLayoutManager(this,2))

        btn_takePhoto.setOnClickListener {
            takePhoto.launch()
        }

        btn_giveExternalPermission.setOnClickListener {
            //startActivity(Intent(Settings.ACTION_SETTINGS))
            /*startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })*/
            openAppPermission()
        }

        checkStoragePaths()
        createInternalFile()
        requestPermissions()

        //loadPhotosFromExternalStorage()
        loadPhotosFromInternalStorage()
        //refreshAdapter()
        refreshAdapter2()
    }

    fun refreshAdapter(){
        adapter = PhotoAdapter(this, photos as ArrayList<Uri>)
        recyclerView.adapter = adapter
    }
    fun refreshAdapter2(){
        adapter2 = PhotoAdapter2(this, images as ArrayList<Bitmap>)
        recyclerView.adapter = adapter2
    }

    fun checkStoragePaths(){
        val internal_m1 = getDir("custom",0)
        val internal_m2 = filesDir

        val external_m1 = getExternalFilesDir(null)
        val external_m2 = externalCacheDir
        val external_m3 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        Log.d("StorageActivity ", internal_m1.absolutePath)
        Log.d("StorageActivity ", internal_m2.absolutePath)
        Log.d("StorageActivity ", external_m1!!.absolutePath)
        Log.d("StorageActivity ", external_m2!!.absolutePath)
        Log.d("StorageActivity ", external_m3!!.absolutePath)
    }

    //internal

    fun createInternalFile(){
        val fileName = "pdp_internal.txt"
        val file:File

        file = if (isPersistent){

            File(filesDir,fileName)
        }else{

            File(cacheDir,fileName)
        }

        if (!file.exists()){

            try {
                file.createNewFile()
                Toast.makeText(this, "File ${fileName} has been created", Toast.LENGTH_SHORT).show()
            }catch (e:IOException){
                Toast.makeText(this, "File ${fileName} creation failed", Toast.LENGTH_SHORT).show()
            }

        }else{
            Toast.makeText(this, "File ${fileName} already exists", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveInternalFile(data:String){
        val fileName = "pdp_internal.txt"

        try {
            val fileOutputStream:FileOutputStream
            fileOutputStream = if (isPersistent){
                openFileOutput(fileName, MODE_PRIVATE)
            }else{
                val file = File(cacheDir,fileName)
                FileOutputStream(file)
            }
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, "Write to ${fileName} successfull", Toast.LENGTH_LONG).show()
        }catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(this, "Write to ${fileName} failed", Toast.LENGTH_LONG).show()
        }

    }

    fun readInternalFile(){
        val fileName = "pdp_internal.txt"
        try {
            val fileInputStream:FileInputStream
            fileInputStream = if (isPersistent){
                openFileInput(fileName)
            }else{
                val file = File(cacheDir,fileName)
                FileInputStream(file)
            }
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines:MutableList<String> = ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line  = reader.readLine()
            while (line != null){
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n",lines)
            Toast.makeText(this, "Read from file ${fileName} successfull: $readText", Toast.LENGTH_SHORT).show()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
            Toast.makeText(this, "Read from file ${fileName} failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteInternalFile(){
        val fileName = "pdp_internal.txt"
        val file:File
        file = if (isPersistent){
            File(cacheDir, fileName)
        }else{
            File(cacheDir,fileName)
        }

        if (file.exists()){
            file.delete()
            Toast.makeText(this, "File ${fileName} has been deleted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "File ${fileName} doesn't exist", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestPermissions(){
        val hasReadPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasLocationPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29
        locationPermissionGranted = hasLocationPermission
        cameraPermissionGranted = hasCameraPermission

        val permissionToRequest = mutableListOf<String>()
        if(!readPermissionGranted)
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if(!writePermissionGranted)
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(!locationPermissionGranted)
            permissionToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if(!cameraPermissionGranted)
            permissionToRequest.add(Manifest.permission.CAMERA)


        if (permissionToRequest.isNotEmpty())
            permissionlauncher.launch(permissionToRequest.toTypedArray())
    }

    private var permissionlauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
        writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: locationPermissionGranted
        cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: cameraPermissionGranted

        if (readPermissionGranted) Toast.makeText(this, "READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show() else btn_giveExternalPermission.visibility = View.VISIBLE
        if (writePermissionGranted) Toast.makeText(this, "WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show() else btn_giveExternalPermission.visibility = View.VISIBLE
        if (locationPermissionGranted) Toast.makeText(this, "ACCESS_FINE_LOCATION", Toast.LENGTH_SHORT).show()
        if (cameraPermissionGranted) Toast.makeText(this, "CAMERA", Toast.LENGTH_SHORT).show()

    }

    //external

    fun saveExternalFile(data:String){
        val fileName = "pdp_external.txt"
        val file:File
        file = if (isPersistent){
            File(getExternalFilesDir(null),fileName)
        }else{
            File(externalCacheDir,fileName)
        }

        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, "Write to ${fileName} successfull", Toast.LENGTH_SHORT).show()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
            Toast.makeText(this, "Write to ${fileName} failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun readExternalFile(){
        val fileName = "pdp_external.txt"
        val file:File
        file = if (isPersistent){
            File(getExternalFilesDir(null),fileName)
        }else{
            File(externalCacheDir,fileName)
        }
        try {
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream,Charset.forName("UTF-8"))
            val lines:MutableList<String> = java.util.ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null){
                lines.add(line)
                line = reader.readLine()
            }

            val readText = TextUtils.join("\n",lines)
            Toast.makeText(this, "Read from file ${fileName} successfull: $readText", Toast.LENGTH_SHORT).show()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
            Toast.makeText(this, "Read from file ${fileName} failed", Toast.LENGTH_SHORT).show()
        }

    }

    fun deleteExternalFile(){
        val fileName = "pdp_external.txt"
        val file:File
        file = if (isPersistent){
            File(getExternalFilesDir(null),fileName)
        }else{
            File(externalCacheDir,fileName)
        }

        if (file.exists()){
            file.delete()
            Toast.makeText(this, "File ${fileName} has been deleted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "File ${fileName} doesn't exist", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){ bitmap ->
        val fileName = UUID.randomUUID().toString()

        val isPhotoSaved = if (isInternal){
            savePhotoToInternalStorage(fileName, bitmap!!)
        }else{
            if (writePermissionGranted){
              savePhotoToExternalStorage(fileName, bitmap!!)
            }else{
                false
            }
        }

        if (isPhotoSaved){
            Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
        }
    }

    fun savePhotoToInternalStorage(fileName:String,bmp:Bitmap):Boolean{
        return try{
            openFileOutput("$fileName.jpg", MODE_PRIVATE).use { stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG,95,stream)){
                    throw IOException("Couldn't save bitmap")
                }
            }
            true
        }catch (e:IOException){
            e.printStackTrace()
            false
        }
    }

    fun savePhotoToExternalStorage(fileName: String,bmp: Bitmap):Boolean{
        val collection  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else{
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"$fileName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            put(MediaStore.Images.Media.WIDTH,bmp.width)
            put(MediaStore.Images.Media.WIDTH,bmp.height)
        }

        return try {
            contentResolver.insert(collection,contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG,95,outputStream)){
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        }catch (e:IOException){
            e.printStackTrace()
            false
        }
    }

    private fun openAppPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri

        startActivityForResult(intent, APP_PERMISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == APP_PERMISSION_CODE) {
            // Here we check if the user granted the permission or not using
            //Manifest and PackageManager as usual
            checkPermissionIsGranted()
        }

    }

    private fun checkPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED){
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
            btn_giveExternalPermission.visibility = View.GONE
        } else{
            Toast.makeText(this, "Not granted", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadPhotosFromExternalStorage(): List<Uri> {

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
        )
        //val photos = mutableListOf<Uri>()
        return contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                photos.add(contentUri)
            }
            photos.toList()
        } ?: listOf()
        adapter.notifyDataSetChanged()
    }

    fun loadPhotosFromInternalStorage(): List<Bitmap> {
        val files = filesDir.listFiles()
        return files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
            val bytes = it.readBytes()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            images.add(bmp)
            adapter2.notifyDataSetChanged()
            bmp
        } ?: listOf()
    }
}