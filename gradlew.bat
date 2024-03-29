<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"
    android:orientation="vertical">

    <ImageView
        android:layout_width="150dp"
        android:layout_height="150dp"
        android:layout_gravity="center"
        android:src="@mipmap/uknow"/>
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Please enter a mobile number"
        android:layout_gravity="center"
        android:layout_marginTop="50dp"/>

    <EditText
        android:id="@+id/phonenumberid"
        android:layout_width="match_parent"
        android:layout_marginLeft="24dp"
        android:layout_marginRight="24dp"
        android:layout_height="80dp"
        android:layout_gravity="center_horizontal"
        android:hint="Enter 10 digit mobile number" />

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="24dp"
        android:id="@+id/sendotp"
        android:text="Send OTP"
        android:textColor="#ffffff"
        android:background="@color/colorAccent"
        />
    <ProgressBar
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:visibility="gone"
        android:id="@+id/progressbarid"/>
</LinearLayout>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          