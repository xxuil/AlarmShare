/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except
 * in compliance with the License. A copy of the License is located at
 *
 *   http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.amazonaws.samples.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;



/**
 * Activity that displays the Edit Memo Screen
 */
public class EditUserActivity extends Activity {
    /**
     * The Text Editor
     */

    /**
     * The Memo being edited
     */
    private Document memo;
    @BindView(R.id.UserID) TextView UserID;
    List<Document> memos;
    @BindView(R.id.OtherUserList) ListView listView;
    @BindView(R.id.altUserID) TextView altUserID;

    /**
     * Lifecycle method called when the activity is created
     * @param savedInstanceState the bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Bind the view variables
        ButterKnife.bind(this);

        // If this activity was passed an intent, then receive the noteId of
        // the memo to edit and populate the UI with the content
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            GetItemAsyncTask task = new GetItemAsyncTask();
            task.execute((String) bundle.get("MEMO"));
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        GetUserNameAsync task = new GetUserNameAsync();
        task.execute();
    }

    public void onEditClicked(Document memo) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("MEMO", memo.get("noteId").asString());
        startActivity(intent);
    }
    public void onStarClicked(Document memo) {
        GoldStar task = new GoldStar();
        task.execute();
        ReturnID task2 = new ReturnID();
        task2.execute();
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("MEMO", memo.get("noteId").asString());
        startActivity(intent);
    }

    public void onDeleteClicked(Document memo) {
    }

    /**
     * Event Handler called when the Save button is clicked
     * @param view the initiating view
     */
    public void onSaveClicked(View view) {
//        if (memo == null) {
//            Document newMemo = new Document();
//            newMemo.put("content", eTime.getHour()+":"+eTime.getMinute());
//            newMemo.put("description", eDescript.getText().toString());
//            CreateItemAsyncTask task = new CreateItemAsyncTask();
//            task.execute(newMemo);
//        } else {
//            memo.put("content", eTime.getHour()+":"+eTime.getMinute());
//            memo.put("description", eDescript.getText().toString());
//            UpdateItemAsyncTask task = new UpdateItemAsyncTask();
//            task.execute(memo);
//        }
        // Finish this activity and return to the prior activity
        ReturnID task = new ReturnID();
        task.execute();
        this.finish();
    }
    protected void populateMemoList(List<Document> memos) {
        this.memos = memos;
        listView.setAdapter(new MemoAdapter(this, memos));
    }

    /**
     * Event Handler called when the Cancel button is clicked
     * @param view the initiating view
     */
    public void onCancelClicked(View view) {
        ReturnID task = new ReturnID();
        task.execute();
        this.finish();
    }

    public void onRefreshClicked(View view) {
        PopulateUserList task = new PopulateUserList();
        task.execute();
    }
    /**
     * Async Task to retrieve a Memo by its noteId from the DynamoDB table
     */
    private class GetItemAsyncTask extends AsyncTask<String, Void, Document> {
        @Override
        protected Document doInBackground(String... documents) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditUserActivity.this);
            return databaseAccess.getMemoById(documents[0]);
        }

        @Override
        protected void onPostExecute(Document result) {
            if (result != null) {
                memo = result;
                String eTimes = memo.get("content").asString();
            }
        }
    }

    /**
     * Async Task to create a new memo into the DynamoDB table
     */
    private class CreateItemAsyncTask extends AsyncTask<Document, Void, Void> {
        @Override
        protected Void doInBackground(Document... documents) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditUserActivity.this);
            databaseAccess.create(documents[0]);
            return null;
        }
    }

    private class ReturnID extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditUserActivity.this);
            databaseAccess.returnID(EditUserActivity.this);
            return databaseAccess.getID();
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println("Returned ID");
        }
    }
    private class GetUserNameAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditUserActivity.this);
            return databaseAccess.getID();
        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null){
                UserID.append(s);
            }
        }
    }

    private class GoldStar extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String myID = UserID.getText().toString();
            myID = myID.substring(myID.lastIndexOf(" ")+1);
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditUserActivity.this);
            databaseAccess.setID(EditUserActivity.this,myID);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println("executed");
        }
    }

    private class PopulateUserList extends AsyncTask<Void, Void, List<Document>> {
        @Override
        protected List<Document> doInBackground(Void... params) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditUserActivity.this);
            databaseAccess.setID(EditUserActivity.this,altUserID.getText().toString());
            return databaseAccess.getAllMemos();
        }

        @Override
        protected void onPostExecute(List<Document> documents) {
            if (documents != null) {
                populateMemoList(documents);
            }
        }
    }

    private String shortenString(String text) {
        String temp = text.replaceAll("\n", " ");
        if (temp.length() > 25) {
            return temp.substring(0, 25) + "...";
        } else {
            return temp;
        }
    }
    /**
     * Async Task to save an existing memo into the DynamoDB table
     */
    private class UpdateItemAsyncTask extends AsyncTask<Document, Void, Void> {
        @Override
        protected Void doInBackground(Document... documents) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditUserActivity.this);
            databaseAccess.update(documents[0]);
            return null;
        }
    }
    private class MemoAdapter extends ArrayAdapter<Document> {
        MemoAdapter(Context context, List<Document> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_list_item2, parent, false);
            }

            ImageView btnEdit = (ImageView) convertView.findViewById(R.id.btnEdit);
            ImageView btnDelete = (ImageView) convertView.findViewById(R.id.btnDelete);
            TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            TextView txtMemo = (TextView) convertView.findViewById(R.id.txtMemo);

            // Fill in the text fields
            final Document memo = memos.get(position);
            if(memo.containsKey("description") && memo.containsKey("content")) {
                txtDate.setText(memo.get("description").asString());
                txtMemo.setText(shortenString(memo.get("content").asString()));
            }

            // Wire up the event handlers for the buttons
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEditClicked(memo);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStarClicked(memo);
                }
            });

            return convertView;
        }
    }
}
