package com.example.sqlserverandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class MainActivity : AppCompatActivity() {

    private lateinit var idField: EditText
    private lateinit var inputField: EditText
    private lateinit var saveButton: Button
    private lateinit var fetchButton: Button
    private lateinit var dataTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        idField = findViewById(R.id.idField)
        inputField = findViewById(R.id.inputField)
        saveButton = findViewById(R.id.saveButton)
        fetchButton = findViewById(R.id.fetchButton)
        dataTextView = findViewById(R.id.dataTextView)

        saveButton.setOnClickListener {
            val idInput = idField.text.toString().toInt()
            val userInput = inputField.text.toString()
            if (userInput.isNotEmpty()) {
                saveToDatabase(idInput, userInput)
            } else {
                Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show()
            }
        }

        fetchButton.setOnClickListener {
            fetchFromDatabase()
        }
    }

    private fun saveToDatabase(idInput: Int, userInput: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver")
                val connectionUrl = "jdbc:jtds:sqlserver://192.168.10.5:1433/AndroidAppDB"
                val connection: Connection = DriverManager.getConnection(
                    connectionUrl,
                    "androidapp",
                    "ss"
                )
                val query = "INSERT INTO UserInputs (id, text) VALUES (?, ?)"
                val preparedStatement = connection.prepareStatement(query)
                preparedStatement.setInt(1, idInput) // Set the value for the first placeholder (id)
                preparedStatement.setString(2, userInput) // Set the value for the second placeholder (text)
                preparedStatement.executeUpdate()

                preparedStatement.close()
                connection.close()

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Data saved successfully", Toast.LENGTH_SHORT)
                        .show()
                    inputField.text.clear()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: JDBC driver not found",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    private fun fetchFromDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver")

                val connectionUrl = "jdbc:jtds:sqlserver://192.168.10.5:1433/AndroidAppDB"
                val connection: Connection = DriverManager.getConnection(
                    connectionUrl,
                    "androidapp",
                    "ss"
                )


                val query = "SELECT * FROM UserInputs ORDER BY ID DESC;"
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(query)

                val stringBuilder = StringBuilder()
                while (resultSet.next()) {
                    stringBuilder.append(resultSet.getString("text")).append("\n")
                }

                resultSet.close()
                statement.close()
                connection.close()

                runOnUiThread {
                    if (stringBuilder.isNotEmpty()) {
                        dataTextView.text = stringBuilder.toString()
                    } else {
                        dataTextView.text = "No data found"
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "SQL Error: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: JDBC driver not found",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
