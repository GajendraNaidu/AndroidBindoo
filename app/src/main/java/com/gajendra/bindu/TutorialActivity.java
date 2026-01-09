package com.gajendra.bindu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {

    private TutorialView tutorialView;
    private TextView instructionText;
    private Button skipButton;
    private Button gotItButton;
    private int currentStep = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.BLACK);
        layout.setPadding(40, 40, 40, 40);

        // Instruction text at top
        instructionText = new TextView(this);
        instructionText.setTextColor(Color.WHITE);
        instructionText.setTextSize(18);
        instructionText.setPadding(20, 20, 20, 40);
        instructionText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(instructionText);

        // Tutorial view in center
        tutorialView = new TutorialView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        tutorialView.setLayoutParams(params);
        layout.addView(tutorialView);

        // Button container
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setPadding(20, 40, 20, 20);
        buttonContainer.setGravity(android.view.Gravity.CENTER);

        // Skip button
        skipButton = new Button(this);
        skipButton.setText("Skip");
        skipButton.setTextColor(Color.WHITE);
        skipButton.setBackgroundColor(Color.parseColor("#555555"));
        skipButton.setPadding(40, 20, 40, 20);
        LinearLayout.LayoutParams skipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        skipParams.setMargins(0, 0, 20, 0);
        skipButton.setLayoutParams(skipParams);
        skipButton.setOnClickListener(v -> finishTutorial());
        buttonContainer.addView(skipButton);

        // Got It button (initially hidden)
        gotItButton = new Button(this);
        gotItButton.setText("Got It! Let's Play");
        gotItButton.setTextColor(Color.WHITE);
        gotItButton.setBackgroundColor(Color.parseColor("#22C822"));
        gotItButton.setPadding(40, 20, 40, 20);
        gotItButton.setVisibility(View.GONE);
        gotItButton.setOnClickListener(v -> finishTutorial());
        buttonContainer.addView(gotItButton);

        layout.addView(buttonContainer);

        setContentView(layout);

        startTutorial();
    }

    private void startTutorial() {
        // Step 1: Show how to draw lines
        handler.postDelayed(() -> {
            currentStep = 1;
            instructionText.setText("Tap two adjacent dots to draw a line");
            tutorialView.animateStep1();
        }, 500);

        // Step 2: Show completing a box
        handler.postDelayed(() -> {
            currentStep = 2;
            instructionText.setText("Complete all 4 sides to capture a box!");
            tutorialView.animateStep2();
        }, 4000);

        // Step 3: Show the goal
        handler.postDelayed(() -> {
            currentStep = 3;
            instructionText.setText("Get more boxes than the computer to win!\n\nBonus: Completing a box gives you another turn!");
            tutorialView.animateStep3();

            // Show "Got It" button and hide skip
            skipButton.setVisibility(View.GONE);
            gotItButton.setVisibility(View.VISIBLE);
        }, 8000);
    }

    private void finishTutorial() {
        // Mark tutorial as seen
        SharedPreferences prefs = getSharedPreferences("BinduPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("tutorial_seen", true).apply();

        // Start game
        Intent intent = new Intent(TutorialActivity.this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    // Custom view for drawing the tutorial animation
    class TutorialView extends View {
        private Paint linePaint;
        private Paint boxPaint;
        private Paint textPaint;
        private Bitmap dotCornerBitmap;
        private Bitmap dotEdgeBitmap;
        private Bitmap dotCenterBitmap;
        private Bitmap playerAvatar;
        private Bitmap computerAvatar;

        private float animationProgress = 0f;
        private int currentAnimationStep = 0;

        public TutorialView(Context context) {
            super(context);

            // Load the same dot bitmaps used in the game
            dotCornerBitmap = getBitmapFromVectorDrawable(R.drawable.bindudr);  // top-left corner
            dotEdgeBitmap = getBitmapFromVectorDrawable(R.drawable.binduldr);   // top edge
            dotCenterBitmap = getBitmapFromVectorDrawable(R.drawable.binduldrt); // center (all directions)

            // Load avatar bitmaps
            playerAvatar = getBitmapFromVectorDrawable(R.drawable.player_avatar);
            computerAvatar = getBitmapFromVectorDrawable(R.drawable.computer_avatar);

            linePaint = new Paint();
            linePaint.setColor(Color.parseColor("#22C822"));
            linePaint.setStrokeWidth(12);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeCap(Paint.Cap.ROUND);
            linePaint.setAntiAlias(true);

            boxPaint = new Paint();
            boxPaint.setColor(Color.parseColor("#22C822"));
            boxPaint.setAlpha(100);
            boxPaint.setStyle(Paint.Style.FILL);
            boxPaint.setAntiAlias(true);

            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(48);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);
        }

        private Bitmap getBitmapFromVectorDrawable(int drawableId) {
            android.graphics.drawable.Drawable drawable = androidx.core.content.ContextCompat.getDrawable(getContext(), drawableId);
            if (drawable == null) {
                return null;
            }

            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    android.graphics.Bitmap.Config.ARGB_8888);

            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }

        public void animateStep1() {
            currentAnimationStep = 1;
            animateProgress(0f, 1f, 2000);
        }

        public void animateStep2() {
            currentAnimationStep = 2;
            animateProgress(0f, 1f, 3000);
        }

        public void animateStep3() {
            currentAnimationStep = 3;
            animateProgress(0f, 1f, 2000);
        }

        private void animateProgress(float from, float to, long duration) {
            ValueAnimator animator = ValueAnimator.ofFloat(from, to);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                animationProgress = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            int spacing = 120;

            if (currentAnimationStep == 1) {
                // Step 1: Draw a 2x2 grid and animate drawing one line
                drawDot(canvas, centerX - spacing, centerY - spacing, dotCenterBitmap);
                drawDot(canvas, centerX + spacing, centerY - spacing, dotCenterBitmap);
                drawDot(canvas, centerX - spacing, centerY + spacing, dotCenterBitmap);
                drawDot(canvas, centerX + spacing, centerY + spacing, dotCenterBitmap);

                // Animate drawing a line
                if (animationProgress > 0) {
                    float x1 = centerX - spacing;
                    float y1 = centerY - spacing;
                    float x2 = centerX + spacing;
                    float y2 = centerY - spacing;

                    float currentX = x1 + (x2 - x1) * animationProgress;
                    canvas.drawLine(x1, y1, currentX, y2, linePaint);
                }

            } else if (currentAnimationStep == 2) {
                // Step 2: Show completing a box
                drawDot(canvas, centerX - spacing, centerY - spacing, dotCenterBitmap);
                drawDot(canvas, centerX + spacing, centerY - spacing, dotCenterBitmap);
                drawDot(canvas, centerX - spacing, centerY + spacing, dotCenterBitmap);
                drawDot(canvas, centerX + spacing, centerY + spacing, dotCenterBitmap);

                // Draw 3 sides already
                canvas.drawLine(centerX - spacing, centerY - spacing, centerX + spacing, centerY - spacing, linePaint);
                canvas.drawLine(centerX - spacing, centerY - spacing, centerX - spacing, centerY + spacing, linePaint);
                canvas.drawLine(centerX + spacing, centerY - spacing, centerX + spacing, centerY + spacing, linePaint);

                // Animate the 4th line
                if (animationProgress > 0 && animationProgress < 0.5f) {
                    float progress = animationProgress / 0.5f;
                    float x1 = centerX - spacing;
                    float y1 = centerY + spacing;
                    float x2 = centerX + spacing;
                    float y2 = centerY + spacing;

                    float currentX = x1 + (x2 - x1) * progress;
                    canvas.drawLine(x1, y1, currentX, y2, linePaint);
                } else if (animationProgress >= 0.5f) {
                    // Draw complete box
                    canvas.drawLine(centerX - spacing, centerY + spacing, centerX + spacing, centerY + spacing, linePaint);

                    // Fill box with animation
                    float fillProgress = (animationProgress - 0.5f) / 0.5f;
                    boxPaint.setAlpha((int)(100 * fillProgress));
                    canvas.drawRect(centerX - spacing, centerY - spacing, centerX + spacing, centerY + spacing, boxPaint);
                }

            } else if (currentAnimationStep == 3) {
                // Step 3: Show multiple boxes with scores
                int smallSpacing = 100;

                // Draw player box (green)
                drawCompletedBox(canvas, centerX - smallSpacing - 60, centerY - 60, smallSpacing, Color.parseColor("#22C822"), "YOU", playerAvatar);

                // Draw computer box (orange)
                drawCompletedBox(canvas, centerX + 60, centerY - 60, smallSpacing, Color.parseColor("#FD9B16"), "Computer", computerAvatar);

                // Animate score appearance
                if (animationProgress > 0.3f) {
                    textPaint.setAlpha((int)(255 * Math.min(1, (animationProgress - 0.3f) / 0.3f)));
                    canvas.drawText("1", centerX - smallSpacing - 60 + smallSpacing - 20, centerY - 60 + 40, textPaint);
                    canvas.drawText("1", centerX + 60 + smallSpacing - 20, centerY - 60 + 40, textPaint);
                }
            }
        }

        private void drawDot(Canvas canvas, float x, float y, Bitmap bitmap) {
            if (bitmap != null) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                float bitmapX = x - bitmap.getWidth() / 2f;
                float bitmapY = y - bitmap.getHeight() / 2f;
                canvas.drawBitmap(bitmap, bitmapX, bitmapY, paint);
            }
        }

        private void drawCompletedBox(Canvas canvas, float left, float top, int size, int color, String label, Bitmap avatar) {
            // Draw box outline
            linePaint.setColor(color);
            canvas.drawLine(left, top, left + size, top, linePaint);
            canvas.drawLine(left + size, top, left + size, top + size, linePaint);
            canvas.drawLine(left + size, top + size, left, top + size, linePaint);
            canvas.drawLine(left, top + size, left, top, linePaint);

            // Draw dots
            drawDot(canvas, left, top, dotCenterBitmap);
            drawDot(canvas, left + size, top, dotCenterBitmap);
            drawDot(canvas, left, top + size, dotCenterBitmap);
            drawDot(canvas, left + size, top + size, dotCenterBitmap);

            // Fill box
            boxPaint.setColor(color);
            boxPaint.setAlpha(100);
            canvas.drawRect(left, top, left + size, top + size, boxPaint);

            // Draw avatar in the center of the box
            if (avatar != null) {
                float avatarSize = size * 0.6f; // Avatar is 60% of box size
                float avatarLeft = left + (size - avatarSize) / 2;
                float avatarTop = top + (size - avatarSize) / 2;
                android.graphics.Rect destRect = new android.graphics.Rect(
                    (int)avatarLeft,
                    (int)avatarTop,
                    (int)(avatarLeft + avatarSize),
                    (int)(avatarTop + avatarSize)
                );
                Paint avatarPaint = new Paint();
                avatarPaint.setAntiAlias(true);
                canvas.drawBitmap(avatar, null, destRect, avatarPaint);
            }
        }
    }
}
