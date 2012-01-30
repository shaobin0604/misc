package src.com.zhang.menu;

import java.util.ArrayList;
import java.util.HashMap;

import src.com.zhang.dialog.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

/**
 * 私念(自定义Dialog)
 * 
 * @author Administrator
 * 
 */
public class MyMenu extends Dialog implements OnItemClickListener
{
	private Context context;

	private GridView menuGrid;

	private MenuItem Menu_Item;

	// --------------------------------------.9图片处理
	// private Bitmap bmp_9path;
	//
	// private NinePatch np;

	public MyMenu(Context context, String myMenuStr[], int myMenuBit[],
			MenuItem Menu_Item)
	{
		super(context, R.style.dialog_fullscreen);
		setContentView(R.layout.mymenu);
		this.context = context;
		this.Menu_Item = Menu_Item;
		setProperty();
		// bmp_9path = Tool.CreatImage(context, R.drawable.tool_box_bkg_wood);
		// np = new NinePatch(bmp_9path, bmp_9path.getNinePatchChunk(), null);
		// // 创建一个ninePatch的对象实例，第一个参数是bitmap、第二个参数是byte[]，这里其实要求我们传入
		// // 如何处理拉伸方式，当然我们不需要自己传入，因为“.9.png”图片自身有这些信息数据，
		// // 也就是我们用“9妹”工具操作的信息！ 我们直接用“.9.png”图片自身的数据调用getNinePatchChunk()即可
		// // 第三个参数是图片源的名称，这个参数为可选参数，直接null~就OK~
		// np.getTransparentRegion(new Rect(250, 50, 250 + bmp_9path
		// .getWidth() * 2, 90 + bmp_9path.getHeight() * 2));
		menuGrid = (GridView) this.findViewById(R.id.GridView_toolbar);
		menuGrid.setAdapter(getMenuAdapter(myMenuStr, myMenuBit));
		menuGrid.setOnItemClickListener(this);

		// 设置背景透明度
		View view = findViewById(R.id.mainlayout);
		view.getBackground().setAlpha(0);// 120为透明的比率
		view.setBackgroundResource(R.drawable.tool_box_bkg_wood);// 设置背景图片

		// Dialog按对话框外面取消操作
		this.setCanceledOnTouchOutside(true);

	}

	/**
	 * 设置窗体属性
	 */

	private void setProperty()
	{
		// // TODO Auto-generated method stub
		// window = getWindow();
		// WindowManager.LayoutParams wl = window.getAttributes();
		// // wl.alpha=0.6f;
		// wl.screenBrightness = 1;// 设置当前窗体亮度
		// wl.gravity = Gravity.CENTER_VERTICAL;
		// wl.setTitle("手机操作个性设定");
		// window.setAttributes(wl);

		Window w = getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.alpha = 1.0f;// 设置当前对话框的 透明度

		// lp.dimAmount = 0.0f;// 设置对话框 悬浮的活动类透明度
		// lp.y = 100; // 对话框的显示位置
		// lp.gravity = Gravity.CENTER_VERTICAL;
		w.setAttributes(lp);

	}

	/**
	 * 构造菜单Adapter
	 * 
	 * @param menuNameArray
	 *            名称
	 * @param imageResourceArray
	 *            图片
	 * @return SimpleAdapter
	 */
	public SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray)
	{
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(context, data,
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });
		return simperAdapter;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id)
	{
		Menu_Item.ItemClickListener(position);
		this.dismiss();
	}

}
