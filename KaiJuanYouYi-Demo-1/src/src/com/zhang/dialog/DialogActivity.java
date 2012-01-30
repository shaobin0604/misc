package src.com.zhang.dialog;

import src.com.zhang.menu.MenuItem;
import src.com.zhang.menu.MyMenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

/**
 * 私念
 * 
 * @author Administrator
 * 
 */
public class DialogActivity extends Activity implements MenuItem
{

	private String myMenuStr[] = { "存储卡", "我的下载", "图书导入", "系统备份", "系统恢复",
			"清除全部", "在线升级", "快速入门", "关于开卷", "退出系统" };

	private int myMenuBit[] = { R.drawable.icon_sdcard, R.drawable.icon_sdcard,
			R.drawable.icon_sdcard, R.drawable.icon_sdcard,
			R.drawable.icon_sdcard, R.drawable.icon_sdcard,
			R.drawable.icon_sdcard, R.drawable.icon_sdcard,
			R.drawable.icon_sdcard, R.drawable.icon_sdcard };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	// ------------------------------------------------MENU事件
	/**
	 * 创建MENU
	 */

	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add("menu");// 必须创建一项
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 拦截MENU
	 */

	public boolean onMenuOpened(int featureId, Menu menu)
	{

		new MyMenu(this, myMenuStr, myMenuBit, this).show();
		return false; // 返回为true 则显示系统menu
	}

	@Override
	public void ItemClickListener(int position)
	{
		Toast.makeText(this, "你选中第" + (position + 1) + "个", Toast.LENGTH_SHORT)
				.show();
	}
}