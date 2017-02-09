package com.leo.boom.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leo.boom.R;
import com.leo.boom.modle.Device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 */

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {

    List<BluetoothDevice> mDatas = new ArrayList<>();
    OnItemClickListener mOnItemClickListener;

    public BluetoothDeviceAdapter() {
    }

    @Override
    public BluetoothDeviceAdapter.DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        final DeviceViewHolder holder = new DeviceViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onItemCLick(position, mDatas.get(position), v);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        BluetoothDevice device = mDatas.get(position);
        String name = device.getName();
        holder.tvName.setText(TextUtils.isEmpty(name) ? "no name" : name);
        holder.tvAddress.setText(device.getAddress());
        int state = -1;
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED:
                state = R.string.bond_boned;
                break;
            case BluetoothDevice.BOND_BONDING:
                state = R.string.bond_boning;
                break;
            case BluetoothDevice.BOND_NONE:
                state = R.string.bond_none;
                break;
            default:
                state = R.string.unknown;
                break;
        }
        holder.tvState.setText(state);
    }

    @Override
    public int getItemCount() {
        if (mDatas == null) {
            return 0;
        } else {
            return mDatas.size();
        }
    }

    public void addAll(Collection<BluetoothDevice> devices) {
        mDatas.addAll(devices);
        notifyDataSetChanged();
    }

    public void addItem(BluetoothDevice device) {
        if (mDatas.contains(device)) {
            notifyItemChanged(mDatas.indexOf(device));
        } else {
            mDatas.add(device);
            notifyItemInserted(mDatas.indexOf(device));
        }
    }


    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvAddress;
        TextView tvState;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvAddress = (TextView) itemView.findViewById(R.id.tv_address);
            tvState = (TextView) itemView.findViewById(R.id.tv_state);
        }
    }


    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemCLick(int postion, BluetoothDevice device, View view);
    }
}
