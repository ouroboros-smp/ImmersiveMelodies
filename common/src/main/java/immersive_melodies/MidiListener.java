package immersive_melodies;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.client.Minecraft;

import javax.sound.midi.*;
import java.util.HashSet;
import java.util.Set;

public class MidiListener {
    public static void launch() {
        new Thread(new MidiListenerThread()).start();
    }

    static class MidiListenerThread implements Runnable {
        private final Set<MidiDevice.Info> connectedDevices = new ConcurrentSet<>();

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                try {
                    MidiDevice.Info[] midiDevices = MidiSystem.getMidiDeviceInfo();
                    for (MidiDevice.Info info : midiDevices) {
                        try {
                            if (connectedDevices.contains(info)) continue;
                            MidiDevice device = MidiSystem.getMidiDevice(info);
                            if (device.getMaxTransmitters() != 0) {
                                device.open();
                                Transmitter transmitter = device.getTransmitter();
                                transmitter.setReceiver(new MidiReceiver(connectedDevices, info));

                                Common.LOGGER.info("MIDI Device: {} - {}", info.getName(), info.getDescription());
                                connectedDevices.add(info);
                            }
                        } catch (MidiUnavailableException e) {
                            Common.LOGGER.warn("MIDI Device unavailable: {}", info.getName(), e);
                        }
                    }
                } catch (Exception e) {
                    Common.LOGGER.error("Error initializing MIDI devices", e);
                }
            }
        }
    }

    static class MidiReceiver implements Receiver {
        private final Set<MidiDevice.Info> connectedDevices;
        private final MidiDevice.Info info;
        private final Set<Integer> sustainedNotes = new HashSet<>();
        private boolean sustain;

        public MidiReceiver(Set<MidiDevice.Info> connectedDevices, MidiDevice.Info info) {
            this.connectedDevices = connectedDevices;
            this.info = info;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage sm) {
                int command = sm.getCommand();
                int data1 = sm.getData1();
                int data2 = sm.getData2();

                Minecraft.getInstance().execute(() -> handleMessage(command, data1, data2));
            }
        }

        private void handleMessage(int command, int data1, int data2) {
            if (command == ShortMessage.NOTE_ON) {
                if (data2 == 0) {
                    noteOff(data1);
                } else {
                    if (sustainedNotes.remove(data1)) {
                        Client.playNote(data1, 0);
                    }
                    Client.playNote(data1, data2);
                }
            } else if (command == ShortMessage.NOTE_OFF) {
                noteOff(data1);
            } else if (command == ShortMessage.CONTROL_CHANGE && data1 == 64) {
                sustain = data2 >= 64;
                if (!sustain) {
                    for (int note : sustainedNotes) {
                        Client.playNote(note, 0);
                    }
                    sustainedNotes.clear();
                }
            }
        }

        private void noteOff(int note) {
            if (sustain) {
                sustainedNotes.add(note);
            } else {
                Client.playNote(note, 0);
            }
        }

        @Override
        public void close() {
            connectedDevices.remove(info);
        }
    }
}
