package com.example.myapplication.textview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.Character.UnicodeBlock;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VTextView extends View {
	final String TAG = "duongtv";

	//static params
	private static final String FONT_PATH = "ipam.ttf";
	private static final int FONT_COLOR = Color.BLACK;
	private static final int TOP_SPACE = 18;
	private static final int BOTTOM_SPACE = 18;
	public static final int MAX_PAGE = 1024;
	int x = 1;
	int TITLE_SIZE = 48*x;
	int FONT_SIZE = 32*x;
	int RUBY_SIZE = 16*x;

	private int mMarkedIndex = 0;

	//variables

	Context mContext;

	private Typeface mFace;

	private TextStyle titleStyle;
	private TextStyle bodyStyle;
	private TextStyle rubyStyle;// ルビ描字用

	String text = "eee";
	private String title = "タイトル";

	//private int textIndex = 0;
	static int[] pageIndex = new int[MAX_PAGE];
	private int currentPage = 0;
	public static int totalPage = -1;
	//private boolean isPageEnd = false;
	int imageNum = 0;
	private boolean isNextImage = false;//次に画像がある

	private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

	int width;
	int height;

	private boolean mVertical = false; //default text is drawn horizontal Right to Left and Top to Bottom


	//methods

	public VTextView(Context context){
		this(context , null);
		init(context);
	}

	public VTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public VTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context){
		mContext = context;
		//mFace = Typeface.createFromAsset(context.getAssets(),FONT_PATH);
		mFace = Typeface.create( Typeface.DEFAULT, Typeface.NORMAL);
		//body.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		setFontSize( FONT_SIZE);
		rubyStyle.lineSpace = bodyStyle.lineSpace;
	}

	private boolean clacPageFlag = false;

	public int getMarkedIndex() {
		return mMarkedIndex;
	}

	public void setMarkedIndex(int mMarkIndex) {
		this.mMarkedIndex = mMarkIndex;
	}

	public void setTitle(String title){
		this.title = title;
		this.reset(true);
	}

	public void setText(String text) {
		this.text = text;
		this.reset(true);
	}

	public boolean getVertical(){
		return mVertical;
	}

	public void setVertical(boolean isVertical){
		mVertical = isVertical;
	}
	//set change orientation and calculate page containt marked index
	public int rotate() {
		mVertical = !mVertical;
		try {
			totalPage = (int) mExecutorService.submit(new CalculatorPageCallable(this)).get();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int page = 0;
		while(page < MAX_PAGE - 1 && pageIndex[page + 1] <= mMarkedIndex){
			page++;
		}
		Log.d(TAG, "rotate: currentPage: " + currentPage);
		return page;
	}

	//指定したページにジャンプ。　例外に注意
	public void setPage(int page){
		this.currentPage = page;
		this.invalidate();//再描画する
	}

	public void setCurrentPage(int currentPage){
		this.currentPage = currentPage;
	}

	public int getCurrentPage(){
		return this.currentPage;
	}

	public int getTotalPage(){
		return this.totalPage;
	}

	public void setColor(String fontColor, String backgroundColor){
		titleStyle.paint.setColor(Color.parseColor(fontColor));
		bodyStyle.paint.setColor(Color.parseColor(fontColor));
		rubyStyle.paint.setColor(Color.parseColor(fontColor));
		this.setBackgroundColor(Color.parseColor(backgroundColor));
		reset(true);//設定時に複数回描画が走ってしまう？
	}

	public void setFont( String path ){
		if( path != null) {
			mFace = Typeface.createFromFile( path );

		} else {
			mFace = Typeface.create( Typeface.DEFAULT, Typeface.NORMAL);
		}
		setFontSize( FONT_SIZE ); // テキストのスタイルを初期化
		reset(true);
	}


	public void setFontSize( int size){
		TITLE_SIZE = (int) (size*1.5);
		FONT_SIZE = size;
		RUBY_SIZE = size/2;
		titleStyle = new TextStyle(TITLE_SIZE);
		//titleStyle.paint.setTypeface(Typeface.DEFAULT_BOLD);
		bodyStyle = new TextStyle(FONT_SIZE);
		rubyStyle = new TextStyle(RUBY_SIZE);
		rubyStyle.lineSpace = bodyStyle.lineSpace;
		reset(true);
	}

	public void reset(boolean isReDraw){
		//this.textIndex = 0;
		this.pageIndex[0] = 0;
		this.currentPage = 0;
		this.totalPage = -1;
		//this.isPageEnd = false;
		this.imageNum = 0;
		this.isNextImage = false;
		if(isReDraw) this.invalidate();
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		this.setMeasuredDimension(width, height);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/*
	public void setPageBack(){
		this.currentIndex = (currentIndex>0)? currentIndex-1:0;
		this.invalidate();
	}

	public void setPageForword(){
		if(!isPageEnd) {
			this.currentIndex++;
		}else{
			onPageEnd();
		}
		this.invalidate();
	}
	 */





	public void toggleMode(){
		this.mVertical = !this.mVertical;
		//this.textIndex = pageIndex[currentIndex];
		this.invalidate();
	}

	//半角チェック
	boolean checkHalf(String s){
		CharSetting setting = CharSetting.getSetting(s);
		if ( setting == null){
			return false;
		}else if( s.getBytes().length < 2 && setting.angle == 90.0f){
			return true;
		}
		return false;
	}

	//Character drawing
	public void drawChar(Canvas canvas , String s, PointF pos , TextStyle style, boolean drawEnable){
		CharSetting setting = CharSetting.getSetting(s);
		float fontSpacing = style.fontSpace;//paint.getFontSpacing();
		float halfOffset = 0;//縦書き半角文字の場合の中央寄せ
		//半角チェック　縦書きの場合 座標の基準値の扱いが縦横で変わるので、分割
		if( mVertical && checkHalf( s) ){
			pos.y -= fontSpacing / 2;
		}
		if(mVertical){//縦書き半角文字の場合の中央寄せ
			if ( setting == null && s.getBytes().length < 2 ){
				halfOffset = 0.2f;
			}
		}
		//draw text
		if( drawEnable ){
			if (setting == null || !mVertical) {
				// normal characters
				canvas.drawText(s, pos.x + fontSpacing * halfOffset , pos.y, style.paint);
			} else {
				// special characters
				canvas.save();
				canvas.rotate(setting.angle, pos.x, pos.y);
				canvas.drawText(s,
						pos.x + fontSpacing * setting.x, pos.y + fontSpacing * setting.y,
						style.paint);
				canvas.restore();
			}
		}
		//Half-width check for horizontal writing
		if( !mVertical && checkHalf( s) ){
			pos.x -= fontSpacing / 2;
		}
	}
	//String drawing function
	public boolean drawString(Canvas canvas , String s, PointF pos, TextStyle style , boolean drawEnable){
		Log.d("drawString", "drawString: "+ s);
		for(int i =0; i< s.length(); i++){
			drawChar(canvas , s.charAt(i)+"", pos, style ,drawEnable);
			if ( !goNext( s, pos , style , true) ){
				return false;
			}
		}
		return true;
	}

	//Line feed processing. True if the next line is written False when the end is reached
	boolean goNextLine(PointF pos , TextStyle type, float spaceRate){
		if(mVertical){
			pos.x -= type.lineSpace * spaceRate;
			pos.y = TOP_SPACE + type.fontSpace;
			if( pos.x > 0 ){
				return true;
			}else {
				return false;
			}
		}else{
			pos.y += type.lineSpace * spaceRate;
			pos.x = TOP_SPACE ;
			if( pos.y < height-TOP_SPACE){
				return true;
			}else {
				return false;
			}
		}
	}

	//Move the cursor to the next position: true if the next line is written false when the end is reached
	boolean goNext(String s, PointF pos , TextStyle type , boolean lineChangable){
		boolean newLine = false;
		if (mVertical){
			if( pos.y + type.fontSpace > height - BOTTOM_SPACE) {
				// もう文字が入らない場合
				newLine = true;
			}
		} else{
			if( pos.x + type.fontSpace > width - BOTTOM_SPACE - type.fontSpace ) {
				// もう文字が入らない場合
				newLine = true;
			}
		}

		if (newLine && lineChangable) {
			// 改行処理
			return goNextLine( pos , type , 1);
		} else {
			// 文字を送る
			float fontSpace = type.fontSpace;
			//if(checkHalf( s )) fontSpace /= 2;
			if(mVertical){
				pos.y += fontSpace;
			}else{
				pos.x += fontSpace;
			}
		}
		return true;
	}

	void initPos(PointF pos){
		if(mVertical){
			pos.x = width - bodyStyle.lineSpace;
			pos.y = TOP_SPACE + bodyStyle.fontSpace;
		}else{
			pos.x = TOP_SPACE ;
			pos.y =  bodyStyle.lineSpace;
		}
	}

	//縦書きと横書きの基準値座標の差を吸収する
	PointF getHeadPos(PointF pos, TextStyle style){
		PointF res = new PointF();
		if(mVertical){
			res.x = pos.x;
			//res.y = pos.y-style.fontSpace;
			res.y = pos.y;
		}else {
			//res.x = pos.x-style.fontSpace;
			res.x = pos.x;
			res.y = pos.y;
		}
		return res;
	}

	PointF getRubyPos(CurrentState state){
		PointF res = new PointF();
		if(mVertical){
			res.x = state.rubyStart.x + bodyStyle.fontSpace;//一文字ずらして表示
			res.y = state.rubyStart.y - rubyStyle.fontSpace;//縦書きの場合は基準がずれているため補正
			if( state.pos.y-state.rubyStart.y > 0){ //改行が入っていない場合
				res.y -= 0.5* ( state.rubyText.length()*rubyStyle.fontSpace - (state.pos.y-state.rubyStart.y));
			}
			if( res.y < TOP_SPACE) res.y = TOP_SPACE;
		}else{
			res.x = state.rubyStart.x ;
			res.y = state.rubyStart.y- bodyStyle.fontSpace;//一文字ずらして表示
			if( state.pos.x-state.rubyStart.x > 0){ //改行が入っていない場合
				res.x -= 0.5* ( state.rubyText.length()*rubyStyle.fontSpace - (state.pos.x-state.rubyStart.x) );
			}
		}

		return res;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.textDraw(canvas , currentPage,true, this);
		Log.d(TAG,"onDraw VTextView: "+ currentPage);
		/*if( this.totalPage < 0 ) {
			this.calcPages();
		}*/
	}

	public void calcPages() {
		try {
			totalPage = (int)mExecutorService.submit(new CalculatorPageCallable(this)).get();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if( onPageCalculateListener != null) onPageCalculateListener.onPageClac(totalPage);
	}

	class PageCountTask extends AsyncTask<Long, Integer, Double> {
		@Override
		protected Double doInBackground(Long... params) {
			int current = 0;
			Log.d("page",current+"");
			while( !textDraw( null , current , false, null) ){
				//描画を無効化して最後のページになるまで進める。
				current++;
			}
			totalPage = current-1;
			return null;
		}

		@Override
		protected void onPostExecute(Double result) {
			if( onPageCalculateListener != null) onPageCalculateListener.onPageClac(totalPage);
		}
	};



	public boolean drawPage(Canvas canvas ,int page , View v){
		//this.currentIndex = page;
		return textDraw(canvas ,page, true , v);
	}

	//そのページが挿絵かどうか判定し、挿絵ならURLを返し、次のページの開始点を更新する。
	public String checkImage( int page ){
		int index = pageIndex[page];

		if( index < 0 ){
			String urlStr = "";
			URL url;
			int i;
			for(  i = -index; i < text.length(); i++ ){
				if(  text.charAt(i) == '$') break;
				urlStr += text.charAt(i);
			}
			Log.d( "url", urlStr );

			pageIndex[page+1] = i+1;

			return urlStr;
		}

		return null;
	}
	public boolean textDraw(Canvas canvas ,int page, boolean enable , View v){
		//Log.d(TAG, "textDraw: page:  " + page);
		CurrentState state = new CurrentState();
		initPos(state.pos);
		initPos(state.rpos);
		//テキスト位置を初期化
		//Log.d("debug", "width:"+width);
		boolean endFlag =true;

		state.isDrawEnable = enable;
		//draw title of page 0
		/*if(page == 0){
			//Log.d(TAG, "textDraw: 0:  ");
			state.isTitle=true;
			state.isRubyEnable = false;
			state.sAfter ="";//タイトルの時は先読みはしない
			for ( int i =0; i < title.length(); i++) {
				state.lineChangable = true;
				state.str = title.charAt(i)+"";
				//Log.d(TAG, "drawText: "+ state.str);
				charDrawProcess(canvas, state);
			}
			//Insert a line break and a blank line
			state.str = "\n";
			charDrawProcess(canvas, state);
			//textDrawProcess(canvas, state);
			state.isTitle=false;
			state.isRubyEnable = true;
		}*/

		int index = pageIndex[page];

		//挿絵判定

		if( checkImage( page ) != null ){
			if( pageIndex[page+1] < text.length() ){
				return false;
			} else {
				return true; //draw finish
			}
		}


		//draw all characters
		for ( ; index < text.length(); index++) {
			state.lineChangable = true;
			state.strPrev = state.str;
			state.str = text.charAt(index)+"";
			state.sAfter = ( index+1 < text.length() ) ?
					text.charAt(index+1)+"" : "";
			//Log.d(TAG, "call charDrawProcess:  "+index);
			if ( !charDrawProcess(canvas, state) ){
				endFlag = false;
				break;
			}
		}
		//process break page
		if( state.hasImage ){
			pageIndex[page+1] = -(index+2);//負なら挿絵 %$URL$
		}else{
			pageIndex[page+1] = index+1;
		}

		return endFlag;
	}

	int num = 0;
	boolean charDrawProcess(Canvas canvas, CurrentState state){
		num++;
		//process special symbol
		// "%$"が描画されていれば挿絵
		//Log.d(TAG, "charDrawProcess: "+num+ "  ====  "+state.str);

		//stop drawing if there is an image
		if( state.str.equals("%") && state.sAfter.equals("$") ){
			this.isNextImage = true;
			state.hasImage = true;
			return false;
		}

		//ルビが振られている箇所とルビ部分の判定
		if( state.isRubyEnable ){
			if( state.isRubyBody && (state.bodyText.length()>20 || state.str.equals("\n")) ){
				drawString(canvas, state.buf+state.bodyText, state.pos, bodyStyle , state.isDrawEnable);
				state.bodyText = "";
				state.buf ="";
				state.isRubyBody = false;
			}

			if( state.str.equals("|") || state.str.equals("｜")){			//ルビ本体開始
				//ルビ開始中にルビ開始した場合は出力
				if(state.bodyText.length()>0){
					drawString(canvas, state.buf+state.bodyText, state.pos, bodyStyle , state.isDrawEnable);
					state.bodyText = "";
					state.buf ="";
				}
				state.bodyText = "";
				state.buf =state.str;
				state.isRubyBody = true;
				state.rubyStart = getHeadPos( state.pos, bodyStyle);
				return true;
			}
			if ( state.str.equals("《") && 	//ルビ開始
					(state.isRubyBody||state.isKanjiBlock) ){ //ルビ開始状態であれば
				state.isRuby = true;
				state.isRubyBody = false;
				state.rubyText = "";
				return true;
			}
			if ( state.str.equals("》") && state.isRuby ){	//page end
				drawString(canvas, state.bodyText, state.pos, bodyStyle , state.isDrawEnable);
				state.rpos = getRubyPos( state);
				drawString(canvas, state.rubyText, state.rpos, rubyStyle , state.isDrawEnable);
				state.isRuby = false;
				state.bodyText = "";
				state.buf ="";
				if( state.isPageEnd ){
					return false;
				}
				return true;
			}

			//Kanji judgment must be done after ruby start judgment
			boolean isKanji = ( UnicodeBlock.of(state.str.charAt(0))  == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS );
			//Log.d("kanji",state.str +":" + isKanji+state.isKanjiBlock);
			if( isKanji && !state.isKanjiBlock ){
				//漢字が始まったら漢字ブロックフラグを立てる
				//　｜のルビ本体の中でなければ
				if( !state.isRubyBody ){
					state.rubyStart = getHeadPos( state.pos, bodyStyle);
				}
			}
			state.isKanjiBlock = isKanji;

			if( state.isRuby ){
				state.rubyText += state.str;
				return true;
			}
			if(state.isRubyBody){
				state.bodyText += state.str;
				return true;
			}
		}
		//Other normal drawing

		//Change style for title
		TextStyle style = state.isTitle? titleStyle:bodyStyle;

		//Line feed processing
		if( state.str.equals("\n")){
			if( state.strPrev.equals("\n") ){
				return  this.goNextLine( state.pos , style , (float) 0.5 );
			}else{
				return  this.goNextLine( state.pos , style , 1 );
			}

		}
		//Draw a character and go to the next
		this.drawChar(canvas , state.str , state.pos , style ,  state.isDrawEnable);

		if( !this.goNext(state.str , state.pos , style , checkLineChangable(state)) ){
			state.isPageEnd = true;
			if( state.isRubyBody ){ //ルビがある場合はルビを描画してから終了
				return true;
			}else{
				return false;
			}
		}
		return true;
	}

	boolean checkLineChangable(CurrentState state){
		if( !state.lineChangable ){//連続で禁則処理はしない
			state.lineChangable = true;
		}else if ( state.sAfter.equals("。") || state.sAfter.equals("、")
				|| state.sAfter.equals("」") || state.sAfter.equals("』")
				|| state.sAfter.equals(")") || state.sAfter.equals("）")
				|| state.sAfter.equals("]") || state.sAfter.equals("］")
				|| state.sAfter.equals("}") || state.sAfter.equals("｝")
				|| state.sAfter.equals("〉") || state.sAfter.equals("】")
				|| state.sAfter.equals("〕")
				|| state.sAfter.equals("，") || state.sAfter.equals("．")
				|| state.sAfter.equals(".") || state.sAfter.equals(",")){
			state.lineChangable = false;
		}
		return state.lineChangable;
	}

	class CurrentState{
		String strPrev;
		String str;
		String sAfter;

		String rubyText = "";//ルビ
		String bodyText = "";//ルビ対象
		String buf = "";//記号の一時保持用
		boolean isTitle = false;
		boolean isDrawEnable = true;
		boolean isRubyEnable = true;
		boolean isRuby = false;
		boolean isKanjiBlock = false;
		boolean isRubyBody = false;
		boolean lineChangable = true;
		boolean isPageEnd = false;
		boolean hasImage = false;

		PointF pos;//カーソル位置
		PointF rpos;//ルビカーソル位置

		PointF rubyStart;
		PointF rubyEnd ;

		CurrentState(){
			strPrev="";
			sAfter="";
			str="";
			pos= new PointF();
			rpos = new PointF();
			rubyStart = new PointF();
			rubyEnd = new PointF();
		}
	}

	class TextStyle{
		public Paint paint;
		float fontSpace;
		float lineSpace;

		TextStyle(int size){
			this.paint = new Paint();
			this.paint.setTextSize(size);
			this.paint.setColor(FONT_COLOR);
			this.paint.setTypeface(mFace);
			this.paint.setAntiAlias(true);
			this.paint.setSubpixelText(true);

			//this.fontSpace = this.paint.getFontSpacing();
			this.fontSpace = size;
			this.lineSpace = this.fontSpace * 2;
		}
	}

	//ページ数計算処理
	//onMeasureが走ってからでないと計算できない為onCreateタイミングでcalcPageしてもダメ

	OnPageCalculateListener onPageCalculateListener;

	public void setOnPageCalculateListener(OnPageCalculateListener onPageCalculateListener){
		this.onPageCalculateListener = onPageCalculateListener;
	}

	public static interface OnPageCalculateListener {
		public void onPageClac(int total);
	}
}
