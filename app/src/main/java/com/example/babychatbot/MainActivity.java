package com.example.babychatbot;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, String> map = new HashMap<>();
    private TextView outputTextView;
    private TextInputEditText inputEditText;
    private TextInputEditText responseEditText;
    private static final String DATA_FILE = "chatbot_data.txt";
    private static final String API_ENDPOINT = "https://api.wolframalpha.com/v2/result";
    private static final String API_KEY = "4Y4JEU-PHGXUA9APE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputTextView = findViewById(R.id.outputTextView);
        inputEditText = findViewById(R.id.inputEditText);
        responseEditText = findViewById(R.id.responseEditText);

        loadDataFromFile();

        Button trainButton = findViewById(R.id.trainButton);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trainBot();
            }
        });

        Button chatButton = findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatBot();
            }
        });

        Button questionButton = findViewById(R.id.questionButton);
        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askMathematicsQuestion();
            }
        });

        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                outputTextView.setText("Hi, Let's Talk!");
            }
        });
    }

    private void trainBot() {
        String input = inputEditText.getText().toString();
        String response = responseEditText.getText().toString();

        map.put(input, response);
        saveDataToFile();

        outputTextView.setText("I have learned a new thing :)");

        inputEditText.setText("");
        responseEditText.setText("");
    }

    private void chatBot() {
        String input = inputEditText.getText().toString();

        if (map.containsKey(input)) {
            String response = map.get(input);
            outputTextView.setText(response);
        } else {
            outputTextView.setText("Sorry, I haven't been trained for this input :(");
        }


        inputEditText.setText("");
    }

//    private void askMathematicsQuestion() {
//        String question = inputEditText.getText().toString();
//
//        try {
//            String answer = solveMathematicsQuestion(question);
//            outputTextView.setText("The answer is: " + answer);
//        } catch (Exception e) {
//            outputTextView.setText("An error occurred: " + e.getMessage());
//        }
//
//
//        inputEditText.setText("");
//    }
//
//    private String solveMathematicsQuestion(String question) throws Exception {
//        String encodedQuestion = URLEncoder.encode(question, "UTF-8");
//        String urlString = API_ENDPOINT + "?appid=" + API_KEY + "&i=" + encodedQuestion;
//        URL url = new URL(urlString);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        InputStream inputStream = connection.getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            response.append(line);
//        }
//        reader.close();
//        return response.toString();
//    }

    private class SolveMathematicsQuestionTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String question = params[0];
            try {
                return solveMathematicsQuestion(question);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred during API request: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // Process the result or update UI
                outputTextView.setText("The answer is: " + result);
                inputEditText.setText("");
            } else {
                // Handle the case when result is null
                outputTextView.setText("An error occurred during API request");
                inputEditText.setText("");
            }
        }
    }

    private void askMathematicsQuestion() {
        String question = inputEditText.getText().toString().trim();
        if (!question.isEmpty()) {
            SolveMathematicsQuestionTask task = new SolveMathematicsQuestionTask();
            task.execute(question);
        } else {
            Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show();
        }
    }

    private String solveMathematicsQuestion(String question) {
        try {
            String encodedQuestion = URLEncoder.encode(question, "UTF-8");
            String urlString = API_ENDPOINT + "?appid=" + API_KEY + "&i=" + encodedQuestion;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            Log.e(TAG, "An error occurred during API request: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void loadDataFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir(), DATA_FILE)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String input = parts[0];
                    String response = parts[1];
                    map.put(input, response);
                } else {

                    Log.w(TAG, "Skipping line due to incomplete data: " + line);
                }
            }
        } catch (IOException e) {

            createNewFile();
        }
    }

    private void createNewFile() {
        File file = new File(getFilesDir(), DATA_FILE);
        try {
            boolean created = file.createNewFile();
            if (created)
                Toast.makeText(this, "New data file created", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Data file already exists", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(getFilesDir(), DATA_FILE)))) {
            for (String input : map.keySet()) {
                String response = map.get(input);
                writer.write(input + "," + response);
                writer.newLine();
            }
            Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
        }
    }
}
