package com.darktiny.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class SlideMenuLayout extends ViewGroup {

	protected static final int TOUCH_STATE_NORMAL = 0x0;
	protected static final int TOUCH_STATE_SCROLLING = 0x1;
	protected static final int TOUCH_STATE_FORECAST = 0x2;
	protected static final float SCROLL_COEFFICIENT = 1.0F;
	protected static final int CLICK_CORRECTION_COEFFICIENT = 2;

	private float mLeftMenuWidth = 0.25F;
	private int mLeftMenuWidthPixels = 0;
	private View mLeftSlideMenu;
	private boolean mLeftSlideMenuEnabled = false;
	private float mRightMenuWidth = 0.75F;
	private int mRightMenuWidthPixels = 0;
	private View mRightSlideMenu;
	private boolean mRightSlideMenuEnabled = false;

	private float mDownMotionX;
	private float mDownMotionY;
	private int mMoveTimesCounter = 0;
	private int mTouchState = TOUCH_STATE_NORMAL;

	private Scroller mScroller;
	private GestureDetector mGestureDetector;

	public SlideMenuLayout(Context context) {
		this(context, null);
	}

	public SlideMenuLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideMenuLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.init();
	}

	private void init() {
		mScroller = new Scroller(getContext());
		mGestureDetector = new GestureDetector(getContext(), new GestureAdapter());
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		}
		this.layoutSlideMenu(getScrollX());
	}

	private void layoutSlideMenu(int offset) {
		if (null != mLeftSlideMenu && mLeftSlideMenu.getVisibility() != View.GONE) {
			mLeftSlideMenu.setLayoutParams(new LayoutParams((int) getLeftMenuWidthF(), getHeight()));
			mLeftSlideMenu.layout(offset, 0, offset + mLeftSlideMenu.getWidth(), getHeight());
		}
		if (null != mRightSlideMenu && mRightSlideMenu.getVisibility() != View.GONE) {
			mRightSlideMenu.setLayoutParams(new LayoutParams((int) getRightMenuWidthF(), getHeight()));
			mRightSlideMenu.layout(getWidth() - mRightSlideMenu.getWidth() + offset, 0, getWidth() + offset, getHeight());
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			LayoutParams layoutParams = view.getLayoutParams();
			int k = getWidth();
			int m = getHeight();
			if (layoutParams != null) {
				if (layoutParams.width > 0)
					k = layoutParams.width;
				if (layoutParams.height > 0)
					m = layoutParams.height;
			}
			view.measure(View.MeasureSpec.makeMeasureSpec(k, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(m, View.MeasureSpec.EXACTLY));
		}
	}

	private float getLeftMenuWidthF() {
		if (mLeftMenuWidthPixels == 0)
			return getWidth() * mLeftMenuWidth;
		else
			return mLeftMenuWidthPixels;
	}

	private float getRightMenuWidthF() {
		if (this.mRightMenuWidthPixels == 0)
			return getWidth() * mRightMenuWidth;
		else
			return mRightMenuWidthPixels;
	}

	public void setLeftMenuWidth(float width) {
		mLeftMenuWidth = width;
		mLeftMenuWidthPixels = 0;
	}

	public void setLeftMenuWidthPixels(int pixels) {
		mLeftMenuWidthPixels = pixels;
		mLeftMenuWidth = 0.0F;
	}

	public void setLeftSlideMenuId(int id) {
		mLeftSlideMenu = findViewById(id);
	}

	public boolean getLeftSlideMenuEnabled() {
		return mLeftSlideMenuEnabled;
	}

	public void setRightMenuWidth(float width) {
		mRightMenuWidth = width;
		mRightMenuWidthPixels = 0;
	}

	public void setRightMenuWidthPixels(int pixels) {
		mRightMenuWidthPixels = pixels;
		mRightMenuWidth = 0.0F;
	}

	public void setRightSlideMenuId(int id) {
		mRightSlideMenu = findViewById(id);
	}

	public boolean getRightSlideMenuEnabled() {
		return mRightSlideMenuEnabled;
	}

	public void openLeftSlideMenu() {
		mLeftSlideMenuEnabled = true;
		smoothHorizontalScrollTo((int) -getLeftMenuWidthF());
	}

	public void openRightSlideMenu() {
		mRightSlideMenuEnabled = true;
		smoothHorizontalScrollTo((int) getRightMenuWidthF());
	}

	public void reset() {
		mLeftSlideMenuEnabled = false;
		mRightSlideMenuEnabled = false;
		smoothHorizontalScrollTo(0);
	}

	private void smoothHorizontalScrollTo(int fx) {
		int dx = fx - mScroller.getFinalX();
		mScroller.startScroll(mScroller.getFinalX(), 0, dx, 0);
		postInvalidate();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			int prevX = getScrollX();
			int currX = mScroller.getCurrX();
			if (prevX != currX) {
				scrollTo(currX, 0);
				layoutSlideMenu(currX);
			}
			requestLayout();
		}
		super.computeScroll();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_MOVE && mTouchState == TOUCH_STATE_SCROLLING)
			return true;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mMoveTimesCounter = 0;
			mDownMotionX = ev.getX();
			mDownMotionY = ev.getY();
			mGestureDetector.onTouchEvent(ev);
			if (getLeftSlideMenuEnabled()) {
				final Rect frame = new Rect();
				mLeftSlideMenu.getHitRect(frame);
				if (!frame.contains((int) (mDownMotionX - getLeftMenuWidthF()), (int) mDownMotionY)) {
					mTouchState = TOUCH_STATE_FORECAST;
					return true;
				}
			} else if (getRightSlideMenuEnabled()) {
				final Rect frame = new Rect();
				mRightSlideMenu.getHitRect(frame);
				if (!frame.contains((int) (mDownMotionX + getRightMenuWidthF()), (int) mDownMotionY)) {
					mTouchState = TOUCH_STATE_FORECAST;
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(ev.getX() - mDownMotionX) > 2 * Math.abs(ev.getY() - mDownMotionY)) {
				mTouchState = TOUCH_STATE_SCROLLING;
				return true;
			}
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
			mMoveTimesCounter++;
			if (TOUCH_STATE_FORECAST == mTouchState && mMoveTimesCounter > CLICK_CORRECTION_COEFFICIENT)
				mTouchState = TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_UP:
			final int state = mTouchState;
			mTouchState = TOUCH_STATE_NORMAL;
			if (TOUCH_STATE_FORECAST == state) {
				reset();
				return true;
			}
			final int pos = getScrollX();
			if (-pos > getLeftMenuWidthF() / 2)
				openLeftSlideMenu();
			else if (pos > getRightMenuWidthF() / 2)
				openRightSlideMenu();
			else
				reset();
			break;
		}
		return mGestureDetector.onTouchEvent(ev);
	}

	class GestureAdapter extends SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			final int dis = (int) (distanceX / SCROLL_COEFFICIENT) + mScroller.getFinalX();
			if (-dis >= getLeftMenuWidthF())
				openLeftSlideMenu();
			else if (dis >= getRightMenuWidthF())
				openRightSlideMenu();
			else
				smoothHorizontalScrollTo(dis);
			return false;
		}
	}
}
