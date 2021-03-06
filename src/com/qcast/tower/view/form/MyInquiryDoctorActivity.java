package com.qcast.tower.view.form;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qcast.tower.R;
import com.qcast.tower.business.Logic;
import com.qcast.tower.business.Me;
import com.qcast.tower.business.structure.MyChatHistoryModel;
import com.qcast.tower.framework.Storage;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.communication.response.Response;
import com.slfuture.carrie.base.json.JSONArray;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.carrie.base.text.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * 我的问诊历史
 */
public class MyInquiryDoctorActivity extends Activity{

    private ArrayList<MyChatHistoryModel> dataList;
    private MyInquiryAdapter adapter;
    private ListView doctorListView;
    private Button return_btn;
    private Button bell_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_myinquiry_doctor);
        doctorListView = (ListView) this.findViewById(R.id.my_doctor_list);
        return_btn = (Button) this.findViewById(R.id.myinquiry_return_btn);
        bell_btn = (Button) this.findViewById(R.id.myinquiry_bell_btn);
        dataList = new ArrayList<MyChatHistoryModel>();
        adapter = new MyInquiryAdapter(this,dataList);
        doctorListView.setAdapter(adapter);
        loadData();
        return_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MyInquiryDoctorActivity.this.finish();
            }
        });

        doctorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(null == Logic.token) {
                    Toast.makeText(MyInquiryDoctorActivity.this, "请先登录", Toast.LENGTH_LONG).show();
                    return;
                }
                MyChatHistoryModel model = dataList.get(position);
                Me.instance.doChat(MyInquiryDoctorActivity.this, model.imGroupId, model.imUsername);
            }
        });
    }

    private void loadData() {
        Networking.doCommand("queryPipe", new CommonResponse<String>() {
            @Override
            public void onFinished(String content) {
                if (Response.CODE_SUCCESS != code()) {
                    Toast.makeText(MyInquiryDoctorActivity.this, "网络问题", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject resultObject = JSONObject.convert(content);
                if (((JSONNumber) resultObject.get("code")).intValue() <= 0) {
                    Toast.makeText(MyInquiryDoctorActivity.this, ((JSONString) resultObject.get("msg")).getValue(), Toast.LENGTH_LONG).show();
                    return;
                }
                resultObject = (JSONObject) resultObject.get("data");
                JSONArray result = (JSONArray) resultObject.get("records");
                if (null == result) {
                    return;
                }
                for (IJSON item : result) {
                    JSONObject newJSONObject = (JSONObject) item;
                    MyChatHistoryModel myChatHistoryModel = new MyChatHistoryModel();
                    String imageUrl = "";
                    String photoName = "";
                    if (newJSONObject.get("doctorPortrait") != null) {
                        imageUrl = ((JSONString) newJSONObject.get("doctorPortrait")).getValue();
                        photoName = Storage.getImageName(imageUrl);
                        myChatHistoryModel.imageUrl = photoName;
                    }
                    myChatHistoryModel.docName = ((JSONString) newJSONObject.get("doctorName")).getValue();
                    SimpleDateFormat sdf= new SimpleDateFormat("MM/dd/yyyy");
                    long time = ((JSONNumber) newJSONObject.get("time")).longValue();
                    myChatHistoryModel.time = sdf.format(time);
                    myChatHistoryModel.docId=((JSONString) newJSONObject.get("doctor")).getValue();
                    if(newJSONObject.get("topic")!=null) {
                        myChatHistoryModel.topic = ((JSONString) newJSONObject.get("topic")).getValue();
                    }else{
                        myChatHistoryModel.topic="";
                    }
                    myChatHistoryModel.status=((JSONNumber) newJSONObject.get("time")).intValue();
                    myChatHistoryModel.userId=((JSONString) newJSONObject.get("user")).getValue();
                    myChatHistoryModel.id=((JSONNumber) newJSONObject.get("id")).intValue();
                    myChatHistoryModel.photoName = photoName;
                    myChatHistoryModel.imGroupId = ((JSONString) newJSONObject.get("groupId")).getValue();
                    myChatHistoryModel.imUsername = ((JSONString) newJSONObject.get("doctorImUsername")).getValue();

                    dataList.add(myChatHistoryModel);
                    if (!TextUtils.isEmpty(imageUrl) && Text.isBlank(imageUrl)) {
                        continue;
                    }
                    // 加载图片
                    if (!TextUtils.isEmpty(photoName)) {
                        Networking.doImage("image", new ImageResponse(photoName, dataList.size() - 1) {
                            @Override
                            public void onFinished(Bitmap content) {
                                adapter.notifyDataSetChanged();
                            }
                        }, imageUrl);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        }, Logic.token);
    }

    class MyInquiryAdapter extends BaseAdapter{
        private Context context;
        private ArrayList<MyChatHistoryModel> data;

        public MyInquiryAdapter(Context context,ArrayList<MyChatHistoryModel> data){
            this.context=context;
            this.data=data;
        }


        @Override
        public int getCount() {
            if(data!= null && data.size()>0){
                return data.size();
            }else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            if(data!= null && data.size()>position) {
                return data.get(position);
            }else{
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyChatHistoryModel model=data.get(position);
            ViewHolder viewHolder;
            if(convertView==null){
                convertView = LayoutInflater.from(context).inflate(R.layout.my_inquriy_doctor_list_item,null);
                viewHolder = new ViewHolder();
                viewHolder.doctor_name_tv = (TextView)convertView.findViewById(R.id.doctor_name_tv);
                viewHolder.chatTime = (TextView)convertView.findViewById(R.id.myinquiry_date_tv);
                viewHolder.doctor_photo_image = (ImageView)convertView.findViewById(R.id.doctor_photo_image);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.doctor_name_tv.setText(model.docName);
            viewHolder.chatTime.setText(model.time);
            if(model.getPhoto() instanceof Bitmap) {
                viewHolder.doctor_photo_image.setImageBitmap(model.getPhoto());
            }
            return convertView;
        }

        class ViewHolder{
            public TextView doctor_name_tv;
            public TextView chatTime;
            public ImageView doctor_photo_image;
        }
    }
}
