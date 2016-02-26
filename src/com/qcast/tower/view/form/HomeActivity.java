package com.qcast.tower.view.form;

import java.util.ArrayList;
import java.util.HashMap;

import com.qcast.tower.Program;
import com.qcast.tower.R;
import com.qcast.tower.business.Logic;
import com.qcast.tower.business.Me;
import com.qcast.tower.business.Profile;
import com.qcast.tower.business.core.IMeListener;
import com.qcast.tower.framework.Storage;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.communication.response.Response;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.slfuture.carrie.base.json.JSONArray;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.type.core.ITable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HeaderViewListAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SimpleAdapter.ViewBinder;

/**
 * 首页
 */
@ResourceView(id = R.layout.activity_home)
public class HomeActivity extends FragmentEx implements IMeListener {
	public class GridViewAdapter extends BaseAdapter {
		public class ViewHolder {
			ImageView image;
			TextView text;
		}

		/**
		 * 上下文
		 */
		private Context context = null;

		public GridViewAdapter(Context context) {
			this.context = context;
		}
		@Override
		public int getCount() {
			return 6;
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(null == convertView) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.listitem_entry, null);
				holder.image = (ImageView) convertView.findViewById(R.id.entry_image_icon);
				holder.text = (TextView) convertView.findViewById(R.id.entry_label_title);
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			switch(position) {
			case 0:
				holder.image.setImageResource(R.drawable.icon_entry_1);
				holder.text.setText("私人医生");
				break;
			case 1:
				holder.image.setImageResource(R.drawable.icon_entry_2);
				holder.text.setText("预约体检");
				break;
			case 2:
				holder.image.setImageResource(R.drawable.icon_entry_3);
				holder.text.setText("健康档案");
				break;
			case 3:
				holder.image.setImageResource(R.drawable.icon_entry_4);
				holder.text.setText("预约理疗");
				break;
			case 4:
				holder.image.setImageResource(R.drawable.icon_entry_5);
				holder.text.setText("预约挂号");
				break;
			case 5:
				holder.image.setImageResource(R.drawable.icon_entry_6);
				holder.text.setText("自我诊断");
				break;
			}
			return convertView;
		}
	}


	/**
	 * 消息ID
	 */
	public final static int MESSAGE_REGION = 1;

	/**
	 * 当前资讯列表
	 */
	protected ArrayList<HashMap<String, Object>> newsList = new ArrayList<HashMap<String, Object>>();
    
	/**
	 * 顶部浏览器
	 */
	@ResourceView(id = R.id.home_browser)
	public WebView browser;
	/**
	 * 小区选择按钮
	 */
	@ResourceView(id = R.id.home_button_region)
	public Button btnRegion;
	/**
	 * 铃铛
	 */
	@ResourceView(id = R.id.home_button_notify)
	public ImageView btnBell;
	/**
	 * 搜索按钮
	 */
	@ResourceView(id = R.id.home_button_search)
	public Button btnSearch;
	/**
	 * 入口滚动条
	 */
	@ResourceView(id = R.id.home_scroll_entry)
	public HorizontalScrollView scrollEntry;
	/**
	 * 入口表格
	 */
	@ResourceView(id = R.id.home_grid_entry)
	public GridView gridEntry;
	/**
	 * 新闻列表
	 */
	@ResourceView(id = R.id.home_list_news)
	public ListView listNews;
	/**
	 * 当前页面索引
	 */
	protected int page = 1;


	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prepare();
		dealNews();
		dealRegion();
		dealSearch();
		dealEntry();
		//
		loadVersion();
		loadNews();
	}

	@Override
	public void onResume() {
		super.onResume();
		final Button regionButton = (Button) this.getActivity().findViewById(R.id.home_button_region);
		if(null != Logic.regionName) {
			regionButton.setText(fetchRegionName());
		}
		if(Logic.hasMessage) {
			shakeBell();
		}
		else {
			stopBell();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopBell();
	}

	/**
	 * 加载版本准备升级
	 */
	private void loadVersion() {
		Host.doCommand("readVersion", new CommonResponse<String>() {
			@Override
			public void onFinished(String content) {
				if(Response.CODE_SUCCESS != code()) {
					return;
				}
				JSONObject resultObject = JSONObject.convert(content);
				if(((JSONNumber) resultObject.get("code")).intValue() <= 0) {
					return;
				}
				JSONObject result = (JSONObject) resultObject.get("data");
				String version = ((JSONString) (result.get("appVersion"))).getValue();
				String url = ((JSONString) (result.get("downloadUrl"))).getValue();
				int v = Integer.parseInt(version.replace(".", ""));
				if(v <= Integer.valueOf(Program.VERSION.replace(".", ""))) {
					return;
				}
				Intent intent = new Intent(HomeActivity.this.getActivity(), WebActivity.class);
				intent.putExtra("url", url);
				HomeActivity.this.startActivity(intent);
			}
		});
	}

	/**
	 * 加载资讯列表
	 */
	private void loadNews() {
		Host.doCommand("news", new CommonResponse<String>(page) {
			@Override
			public void onFinished(String content) {
				if(Response.CODE_SUCCESS != code()) {
					Toast.makeText(HomeActivity.this.getActivity(), "加载资讯失败", Toast.LENGTH_LONG).show();
					return;
				}
				JSONObject resultObject = JSONObject.convert(content);
				if(((JSONNumber) resultObject.get("code")).intValue() <= 0) {
					Toast.makeText(HomeActivity.this.getActivity(), ((JSONString) resultObject.get("msg")).getValue(), Toast.LENGTH_LONG).show();
					return;
				}
				JSONArray result = (JSONArray) resultObject.get("data");
				int thisPage = (Integer) this.tag;
				if(page != thisPage) {
					return;
				}
				if(0 == result.size()) {
					return;
				}
				for(IJSON item : result) {
					JSONObject newJSONObject = (JSONObject) item;
					HashMap<String, Object> newsMap = new HashMap<String, Object>();
					String photoURL = ((JSONString) newJSONObject.get("imageUrl")).getValue();
					String photoName = Storage.getImageName(photoURL);
					newsMap.put("photo", photoName);
					newsMap.put("title", ((JSONString) newJSONObject.get("title")).getValue());
					newsMap.put("publisher", ((JSONString) newJSONObject.get("publisher")).getValue());
					newsMap.put("date", ((JSONString) newJSONObject.get("date")).getValue());
					newsMap.put("url", ((JSONString) newJSONObject.get("url")).getValue());
					newsList.add(newsMap);
					if(Text.isBlank(photoURL)) {
		            	continue;
		            }
		            // 加载图片
		            Host.doImage("image", new ImageResponse(photoName, newsList.size() - 1) {
						@Override
						public void onFinished(Bitmap content) {
							HashMap<String, Object> map = newsList.get((Integer) tag);
							map.put("photo", content);
							ListView listview = (ListView) HomeActivity.this.getActivity().findViewById(R.id.home_list_news);
							SimpleAdapter adapter = (SimpleAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter();
							adapter.notifyDataSetChanged();
						}
		            }, photoURL);
				}
				ListView listview = (ListView) HomeActivity.this.getActivity().findViewById(R.id.home_list_news);
				SimpleAdapter adapter = (SimpleAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter();
				adapter.notifyDataSetChanged();
				page = thisPage + 1;
			}
		}, page);
	}

	/**
	 * 处理区域按钮
	 */
	public void dealRegion() {
		btnRegion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this.getActivity(), RegionActivity.class);
				HomeActivity.this.startActivityForResult(intent, MESSAGE_REGION);
			}
		});
		btnBell.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null == Me.instance) {
					return;
				}
				Intent intent = new Intent();
				intent.setClass(HomeActivity.this.getActivity(), MyMessagesActivity.class);
				HomeActivity.this.getActivity().startActivity(intent);
			}
		});
		if(null == Profile.instance().region) {
			Intent intent = new Intent(HomeActivity.this.getActivity(), RegionActivity.class);
			HomeActivity.this.startActivityForResult(intent, MESSAGE_REGION);
		}
		else {
			btnRegion.setText(fetchRegionName());
		}
	}

	/**
	 * 处理入口按钮
	 */
	public void dealEntry() {
		if(null == Me.instance) {
			browser.loadUrl(Host.fetchURL("home", ""));
		}
		else {
			browser.loadUrl(Host.fetchURL("home", Me.instance.token));
		}
		scrollEntry.setHorizontalScrollBarEnabled(false);
		DisplayMetrics metrics = new DisplayMetrics();
		this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		GridViewAdapter adapter = new GridViewAdapter(this.getActivity());
		gridEntry.setAdapter(adapter);
		LayoutParams params = new LayoutParams(adapter.getCount() * metrics.widthPixels / 4, LayoutParams.WRAP_CONTENT);
		gridEntry.setLayoutParams(params);
		gridEntry.setColumnWidth(metrics.widthPixels / 4);
		gridEntry.setStretchMode(GridView.NO_STRETCH);
		gridEntry.setNumColumns(adapter.getCount());
		gridEntry.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}
		});
	}

	/**
	 * 处理搜索按钮
	 */
	public void dealSearch() {
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this.getActivity(), SearchActivity.class);
				HomeActivity.this.getActivity().startActivity(intent);
			}
		});
	}
	
	/**
	 * 界面预设
	 */
	private void prepare() {
		if(listNews.getHeaderViewsCount() > 0) {
			return;
		}
		View viewHead = LayoutInflater.from(this.getActivity()).inflate(R.layout.div_home_head, null);
		listNews.addHeaderView(viewHead);
		scrollEntry = (HorizontalScrollView) viewHead.findViewById(R.id.home_scroll_entry);
		gridEntry = (GridView) viewHead.findViewById(R.id.home_grid_entry);
		btnRegion.getBackground().setAlpha(200);
		btnSearch.getBackground().setAlpha(200);
		btnBell.setImageAlpha(200);
		browser = (WebView) viewHead.findViewById(R.id.home_browser);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		browser.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Intent intent = new Intent(HomeActivity.this.getActivity(), WebActivity.class);
				intent.putExtra("url", url);
				startActivity(intent);
                browser.pauseTimers();
                return true;
			}
		});
		this.getActivity().findViewById(R.id.home_layout_head).bringToFront();
	}

	/**
	 * 处理资讯
	 */
	private void dealNews() {
		SimpleAdapter listItemAdapter = new SimpleAdapter(this.getActivity(), newsList, R.layout.listview_news,
			new String[]{"photo", "title", "publisher", "date"}, 
	        new int[]{R.id.news_image_photo, R.id.news_label_title, R.id.news_label_publisher, R.id.news_label_date});
		listItemAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView && data instanceof Bitmap) {
                    ImageView imageView = (ImageView)view;
                    Bitmap bitmap = (Bitmap) data;
                    imageView.setImageDrawable(new BitmapDrawable(bitmap));
                    return true;
                }
                return false;
            }
        });
		listNews.setAdapter(listItemAdapter);
		listNews.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
				index = index - 1;
				HashMap<String, Object> newsMap = newsList.get(index);
				Intent intent = new Intent(HomeActivity.this.getActivity(), WebActivity.class);
				intent.putExtra("url", newsMap.get("url").toString());
				HomeActivity.this.startActivity(intent);
            }
		});
		listNews.setOnScrollListener(new OnScrollListener() {    
	        boolean isLastRow = false;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {      
					loadNews();
	                isLastRow = false;      
	            }
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 2) {      
	                isLastRow = true;      
	            }
			}
		});
	}

	/**
	 * 回调
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RadioActivity.RESULT_CANCEL) {
			return;
		}
		if(MESSAGE_REGION == requestCode) {
			int regionId = data.getIntExtra("regionId", 0);
			if(0 == regionId) {
				return;
			}
			btnRegion.setText(fetchRegionName());
		}
	}

	/**
	 * 获取处理后的区域名称
	 * 
	 * @return 处理后的区域名称
	 */
	public String fetchRegionName() {
		if(null == Profile.instance().region) {
			return "选择小区";
		}
		return Profile.instance().region.getShortName();
	}

	/**
	 * 摇晃铃铛
	 */
	public void shakeBell() {
		btnBell.clearAnimation();
		btnBell.setImageResource(R.drawable.bell_active);
		//
		final RotateAnimation animRight = new RotateAnimation(-30, 30f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
		animRight.setDuration(1000);
		final RotateAnimation animLeft = new RotateAnimation(30f, -30f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
		animLeft.setDuration(1000);
		//
		final AnimationListener listener = new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				Log.d("tower", "");
			}
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationEnd(Animation animation) {
				btnBell.clearAnimation();
				animation.setAnimationListener(null);
				if(animation == animRight) {
			        animLeft.setAnimationListener(this);
					btnBell.startAnimation(animLeft);
				}
				else if(animation == animLeft) {
					animRight.setAnimationListener(this);
					btnBell.startAnimation(animRight);
				}
			}
        };
        animRight.setAnimationListener(listener);
        //
        btnBell.startAnimation(animRight);
	}

	/**
	 * 停止摇晃铃铛
	 */
	public void stopBell() {
		btnBell.clearAnimation();
		btnBell.setImageResource(R.drawable.bell_normal);
	}

	@Override
	public void onConflict() {
		
	}

	@Override
	public void onCommand(String from, String action, ITable<String, Object> data) {
		if("notify".equalsIgnoreCase(action)) {
			shakeBell();
		}
	}
}