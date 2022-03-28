package com.example.m6l6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import java.io.*
import java.lang.Exception
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    var isPersistent:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    fun initViews(){
        val btn_saveInternal = findViewById<Button>(R.id.btn_saveInternal)
        val btn_readInternal = findViewById<Button>(R.id.btn_readInternal)
        val btn_saveExternal = findViewById<Button>(R.id.btn_saveExternal)
        val btn_readExternal = findViewById<Button>(R.id.btn_readExternal)
        val btn_takePhoto = findViewById<Button>(R.id.btn_takePhoto)

        btn_saveInternal.setOnClickListener {
            saveInternalFile("PDP Academy")
        }
        btn_readInternal.setOnClickListener {
            readInternalFile()
        }

        checkStoragePaths()
        createInternalFile()
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
}