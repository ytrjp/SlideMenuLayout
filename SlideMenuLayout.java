package com.darktiny.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class SlideMenuLayout extends ViewGroup {

	public static final String TAG = "SlideMenuLayout";

	private int verticalMinDistance = 50;
	private int minVelocity = 6000;

	private float mDownMotionX;
	private float mDownMotionY;

	private float mLeftMenuWidth = 0.25F;
	private int mLeftMenuWidthPixels = 0;
	private View mLeftSlideMenu;
	@SuppressWarnings("unused")
	private boolean mLeftSlideMenuEnabled;
	private float mRightMenuWidth = 0.75F;
	private int mRightMenuWidthPixels = 0;
	private View mRightSlideMenu;
	@SuppressWarnings("unused")
	private boolean mRightSlideMenuEnabled;

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

	public void setLeftSlideMenuEnabled(boolean enabled) {
		mLeftSlideMenuEnabled = enabled;
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

	public void setRightSlideMenuEnabled(boolean enabled) {
		mRightSlideMenuEnabled = enabled;
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
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownMotionX = ev.getX();
			mDownMotionY = ev.getY();
			mGestureDetector.onTouchEvent(ev);
			break;
		case MotionEvent.ACTION_MOVE:
			final float x = ev.getX();
			final float y = ev.getY();
			final int xDiff = (int) Math.abs(x - mDownMotionX);
			final int yDiff = (int) Math.abs(y - mDownMotionY);
			if (xDiff > 2 * yDiff)
				return true;
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			final int pos = getScrollX();
			if (pos < 0) {
				if (-pos > getLeftMenuWidthF() / 2)
					smoothHorizontalScrollTo((int) -getLeftMenuWidthF());
				else
					smoothHorizontalScrollTo(0);
			} else if (pos > 0) {
				if (pos > getRightMenuWidthF() / 2)
					smoothHorizontalScrollTo((int) getRightMenuWidthF());
				else
					smoothHorizontalScrollTo(0);
			}
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
			final int dis = (int) ((distanceX - 0.5) / 1.0) + mScroller.getFinalX();
			if (dis < 0 && Math.abs(dis) > getLeftMenuWidthF())
				smoothHorizontalScrollTo((int) -getLeftMenuWidthF());
			else if (dis > 0 && Math.abs(dis) > getRightMenuWidthF())
				smoothHorizontalScrollTo((int) getRightMenuWidthF());
			else
				smoothHorizontalScrollTo(dis);
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1.getX() - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity)
				smoothHorizontalScrollTo((int) getRightMenuWidthF());
			else if (e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity)
				smoothHorizontalScrollTo((int) -getLeftMenuWidthF());
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}
}
