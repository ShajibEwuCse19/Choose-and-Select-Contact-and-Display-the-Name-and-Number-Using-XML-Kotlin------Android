package com.shajib.chooseandselectcontract

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shajib.chooseandselectcontract.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE = 1
    private val REQUEST_CODE_CONTACT = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listener for the choose contact button [step-1]
        binding.btnChooseContact.setOnClickListener {
            // Check for permission to access contacts
            checkContactPermission()
        }
    }

    // Check for permission to access contacts [step-2]
    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                REQUEST_CODE
            )
        } else {
            chooseContact()
        }
    }

    // Handle the result of the permission request [step-3]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooseContact()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // Handle the result of the contact picker activity [step-5]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CONTACT && resultCode == RESULT_OK) {
            // Get the selected contact
            getContact(data?.data)
        } else {
            Toast.makeText(this, "No Contact Selected", Toast.LENGTH_SHORT).show()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Get the selected contact [step-6]
    @SuppressLint("Range")
    private fun getContact(uri: Uri?) {
        //ContentProvider - provides access to the data in a application
        //ContentResolver - used to query the content provider's data
        var c = uri?.let { uri ->
            this.contentResolver.query(uri, null, null, null, null)
        }
        if (c != null && c.moveToFirst()) {
            // Get the contact id from the uri and use it to query the content provider
            var contactId =
                c.getString(c.getColumnIndex(ContactsContract.Contacts._ID)) //got id from one table
            //using the id in another table to select the data
            var numberCursor = this.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                arrayOf(contactId),
                null
            )

            // Get the contact name and number from the numberCursor
            if (numberCursor != null && numberCursor.moveToFirst()) {
                val name = numberCursor.getString(
                    numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )
                val number = numberCursor.getString(
                    numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
                runOnUiThread {
                    binding.tvName.text = "Contact Name: $name"
                    binding.tvNumber.text = "Contact Number: $number"
                }
            }
        }
    }

    // Launch the contact picker [step-4]
    private fun chooseContact() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE)
        startActivityForResult(intent, REQUEST_CODE_CONTACT)
    }
}