package com.example.mayank.nfcapp.createnfcid

import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.mayank.nfcapp.Constants
import com.example.mayank.nfcapp.R
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcActivity
import java.io.IOException
import java.io.UnsupportedEncodingException

/**
 * Created by Mayank on 16/03/2018.
 */
class CreateNfcId : AppCompatActivity() {

    private val TAG = CreateNfcId::class.java.simpleName

    internal var inputNfcId: EditText? = null
    internal lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_nfc_id)

        inputNfcId = findViewById<EditText>(R.id.edit_text_nfc_id) as EditText
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }


    fun createNfcId(view:View){
        showLogDebug(TAG, "Create Nfc Id button clicked")
        val tag = inputNfcId?.text.toString().trim({ it <= ' ' })
        writeNfcTag(tag)
//        showLogDebug(TAG,"Tag write successfully")
//        Toast.makeText(this, "Id Tag write successfully", Toast.LENGTH_SHORT)
    }

    private fun writeNfcTag(text: String) {
        val intent = intent
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val mimeRecord = createRecord(text)//NdefRecord.createMime("text/plain", text.getBytes(Charset.forName("US-ASCII")));
                ndef.writeNdefMessage(NdefMessage(mimeRecord))
                ndef.close()
                Toast.makeText(this, "Nfc Id created successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Constants.showLogDebug(TAG, "IOException $e")
                Toast.makeText(this,"Attach Id card and try again!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: FormatException) {
                Constants.showLogDebug(TAG, "Format Exception $e")
                e.printStackTrace()
            }

        } else {
            showLogDebug(TAG,"Ndef is null")
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun createRecord(text: String): NdefRecord {
        val lang = "en"
        val textBytes = text.toByteArray()
        val langBytes = lang.toByteArray(charset("US-ASCII"))
        val langLength = langBytes.size
        val textLength = textBytes.size
        val payload = ByteArray(1 + langLength + textLength)

        // set status byte (see NDEF spec for actual bits)
        payload[0] = langLength.toByte()

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength)
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength)

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }

}