package byow.Core;

import byow.TileEngine.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Area {
    private Area leftChild;
    private Area rightChild;
    private static final int MINHEIGHT = 8; //make it 8
    private static final int MINWIDTH = 8;  // make it 8

    private static final int MINROOMHEIGHT = 3;

    private static final int MINROOMWIDTH = 3;

    private Room room;

    private ArrayList<Room> roomsList = new ArrayList<>();
    private boolean splitHorazontal = true;
    private static final int WORLDWIDTH = 80;
    private static final int WORLDHEIGHT = 30;
    private int width;
    private int height;
    private int x;
    private int y;
    private long seed;

    private boolean lightOn = false;

    private Random rand = new Random(seed);

    private ArrayList<Area> areasList = new ArrayList<>();

    public Area(Random rand) {
        width = WORLDWIDTH;
        height = WORLDHEIGHT;
        leftChild = null;
        rightChild = null;
        x = 0;
        y = 0;
        this.rand = rand;
    }

    public Area(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        leftChild = null;
        rightChild = null;
        this.x = x;
        this.y = y;
    }

    public boolean splitHelper() {
        int maxH = 0;
        int maxW = 0;
        if (leftChild != null && rightChild != null) {
            return false;
        }
        if (width > MINWIDTH && height > MINHEIGHT) {
            if (width > height) {
                splitHorazontal = false;
                maxW += this.width - MINWIDTH + 1;
                if (maxW <= MINWIDTH) {
                    return false;
                }
            } else if (height > width) {
                splitHorazontal = true;
                maxH += this.height - MINHEIGHT + 1;
                if (maxH <= MINHEIGHT) {
                    return false;
                }
            }
        } else {
            return false;
        }

        if (splitHorazontal) {
            int randSizeH = RandomUtils.uniform(this.rand, MINHEIGHT, MINHEIGHT + maxH + 1);
            this.leftChild = new Area(width, randSizeH, x, y);
            this.rightChild = new Area(width, height - randSizeH, x, y + randSizeH);
        } else {
            int randSizeW = RandomUtils.uniform(this.rand, MINWIDTH + maxW + 1);
            this.leftChild = new Area(randSizeW, height, x, y);
            this.rightChild = new Area(width - randSizeW, height, x + randSizeW, y);
        }
        return true;
    }

    public void split() {
        Area originalWorldArea = new Area(WORLDWIDTH, WORLDHEIGHT, 0, 0);
        areasList.add(originalWorldArea);
        int i = 0;
        boolean split = true;
        while (split) {
            Area a = areasList.get(i);
            if (a.leftChild == null && a.rightChild == null) {
                if (a.splitHelper()) {
                    areasList.add(a.leftChild);
                    areasList.add(a.rightChild);
                    split = true;
                } else {
                    split = false;
                }
            } else {
                split = false;
            }
            i++;
        }
    }

    public void createRooms(TETile[][] world) {
        // Go through each area in the list of areas and find a random place to make a rectangle
        int buffer = 2;
        for (Area a: areasList) {
            int sizex = RandomUtils.uniform(this.rand, a.x + buffer,
                    a.x + a.width - MINROOMWIDTH - buffer);
            int sizey = RandomUtils.uniform(this.rand, a.y + buffer,
                    a.y + a.height - MINROOMHEIGHT - buffer);
            int roomWidth = RandomUtils.uniform(this.rand, MINROOMWIDTH, a.x + a.width - sizex);
            int roomHeight = RandomUtils.uniform(this.rand, MINROOMHEIGHT, a.y + a.height - sizey);

            if (a.getLeftChild() == null && a.getRightChild() == null) {
                for (int i = sizex; i < sizex + roomWidth; i++) {
                    for (int j = sizey; j < sizey + roomHeight; j++) {
                        world[i][j] = Tileset.FLOOR;
                    }
                }

                Room newRoom = new Room(roomWidth, roomHeight, sizex, sizey);
                a.room = newRoom;
                roomsList.add(newRoom);
            }
            for (Room r: roomsList) {
                int xRoom = r.roomX + 2;
                int yRoom = r.roomY + 2;
                if (world[xRoom][yRoom] == Tileset.FLOOR) {
                    world[xRoom][yRoom] = Tileset.SAND;
                }
            }
        }
        int xWin = RandomUtils.uniform(this.rand, WORLDWIDTH);
        int yWin = RandomUtils.uniform(this.rand, WORLDHEIGHT);
        world[xWin][yWin] = Tileset.MOUNTAIN;
    }

    public void makeHallways(TETile[][] world) {
        // traverse through all rooms
        ArrayList<Room> hasRoom = new ArrayList<>();
        for (Area a : areasList) {
            if (a.room != null) {
                hasRoom.add(a.room);
            }
        }

        for (int i = 0; i < hasRoom.size(); i++) {
            for (int j = 1; j < hasRoom.size(); j++) {
                Room room1 = hasRoom.get(i);
                Room room2 = hasRoom.get(j);
                if (!verticalHall(room1, room2, world) && !horizontalHall(room1, room2, world)) {
                    bendyHalls(room1, room2, world);
                } else {
                    break;
                }
            }
        }
    }

    public boolean verticalHall(Room room1, Room room2, TETile[][] world) {
        Room top;
        Room bottom;
        if (room1.getRoomY() > room2.getRoomY()) {
            top = room1;
            bottom = room2;
        } else {
            top = room2;
            bottom = room1;
        }
        int maxX = Math.min(bottom.roomX + bottom.roomWidth - 1, top.roomX + top.roomWidth - 1);
        int minX = Math.max(bottom.roomX, top.roomX);
        if (maxX - minX >= 0) {
            // make vertical hallway in the x coordinate the rooms have in common
            if (top.roomX <= bottom.roomX + bottom.roomWidth - 1
                    || top.roomX + top.roomWidth - 1 >= bottom.roomX) {
                int point = minX;
                if (maxX != minX) {
                    point = RandomUtils.uniform(this.rand, minX, maxX);
                }
                for (int k = bottom.roomY + bottom.roomHeight; k <= top.roomY; k++) {
                    world[point][k] = Tileset.FLOOR;
                }
            }
        }
        return (maxX - minX > 0);
    }

    public boolean horizontalHall(Room room1, Room room2, TETile[][] world) {
        Room left;
        Room right;
        if (room1.getRoomX() > room2.getRoomX()) {
            left = room2;
            right = room1;
        } else {
            left = room1;
            right = room2;
        }
        int maxY = Math.min(right.roomY + right.roomHeight - 1, left.roomY + left.roomHeight - 1);
        int minY = Math.max(left.roomY, right.roomY);

        if (maxY - minY >= 0) {
            // make horizontal hallway in the y coordinate the rooms have in common
            if (right.roomY + right.roomHeight - 1 >= left.roomY
                    || left.roomY + left.roomHeight - 1 >= right.roomY) {
                int point = minY;
                if (maxY != minY) {
                    point = RandomUtils.uniform(this.rand, minY, maxY);
                }
                for (int k = left.roomX + left.roomWidth; k <= right.roomX; k++) {
                    world[k][point] = Tileset.FLOOR;
                }
            }
        }
        return (maxY - minY > 0);
    }

    public void bendyHalls(Room room1, Room room2, TETile[][] world) {
        if (room1.roomY < room2.roomY && room1.roomX < room2.roomX) {
            int distX = room2.roomX - room1.roomX - room1.roomWidth;
            int pos = rand.nextInt(room1.roomY, room1.roomY + room1.roomHeight);
            int amountRight = rand.nextInt(distX, distX + room2.roomWidth);
            int amountUp = room2.roomY - pos;
            for (int right = 0; right <= amountRight; right++) {
                world[room1.roomX + room1.roomWidth + right][pos] = Tileset.FLOOR;
            }
            for (int up = 0; up < amountUp; up++) {
                world[room1.roomWidth + room1.roomX + amountRight][up + pos] = Tileset.FLOOR;
            }
            //room 2 is top right, room 1 is bottom left
            //choose random point in each distance and add up and right from room1
        } else if (room1.roomY > room2.roomY && room1.roomX > room2.roomX) {
            int distX = room1.roomX - room2.roomX - room2.roomWidth;
            int pos = rand.nextInt(room2.roomY, room2.roomY + room2.roomHeight);
            int amountRight = rand.nextInt(distX, distX + room1.roomWidth);
            int amountUp = room1.roomY - pos;
            for (int right = 0; right <= amountRight; right++) {
                world[room2.roomX + room2.roomWidth + right][pos] = Tileset.FLOOR;
            }
            for (int up = 0; up < amountUp; up++) {
                world[room2.roomWidth + room2.roomX + amountRight][up + pos] = Tileset.FLOOR;
            }
            //room 1 is top right, room 2 is bottom left
        } else if (room1.roomY < room2.roomY && room1.roomX > room2.roomX) {
            int distX = room1.roomX - room2.roomX - room2.roomWidth;
            int pos = rand.nextInt(room2.roomY, room2.roomY + room2.roomHeight);
            int amountRight = rand.nextInt(distX, distX + room1.roomWidth);
            int amountDown = pos - room1.roomHeight - room1.roomY;
            for (int right = 0; right <= amountRight; right++) {
                world[room2.roomX + room2.roomWidth + right][pos] = Tileset.FLOOR;
            }
            for (int down = amountDown; down > 0; down--) {
                world[room2.roomWidth + room2.roomX + amountRight][pos - down] = Tileset.FLOOR;
            }
            //room 2 is top left, room 1 is bottom right
        } else if (room1.roomY > room2.roomY && room1.roomX < room2.roomX) {
            int distX = room2.roomX - room1.roomX - room1.roomWidth;
            int pos = rand.nextInt(room1.roomY, room1.roomY + room1.roomHeight);
            int amountRight = rand.nextInt(distX, distX + room2.roomWidth);
            int amountDown = pos - room2.roomHeight - room2.roomY;
            for (int right = 0; right <= amountRight; right++) {
                world[room1.roomX + room1.roomWidth + right][pos] = Tileset.FLOOR;
            }
            for (int down = amountDown; down > 0; down--) {
                world[room1.roomWidth + room1.roomX + amountRight][pos - down] = Tileset.FLOOR;
            }
            //room 1 is top left, room 2 is bottom right
        }

    }

    public void createLights(TETile[][] world) {
        ArrayList<Room> hasRoom = new ArrayList<>();
        for (Area a : areasList) {
            if (a.room != null) {
                hasRoom.add(a.room);
            }
        }
        for (Room r : hasRoom) {
            int xRoom = r.roomX;
            int yRoom = r.roomY;
            world[xRoom][yRoom] = Tileset.LIGHT;
            for (int i = xRoom - 2; i <= xRoom + 2; i++) {
                for (int j = yRoom - 2; j <= yRoom + 2; j++) {
                    if (i >= WORLDWIDTH || i < 0 || j >= WORLDHEIGHT
                            || j < 0 || world[i][j] == Tileset.WALL) {
                        continue;
                    }
                    if (world[i][j] == Tileset.FLOOR && world[i][j] != Tileset.LIGHTTWO) {
                        world[i][j] = Tileset.LIGHTTHREE;
                    }
                }
            }
            for (int i = xRoom - 1; i <= xRoom + 1; i++) {
                for (int j = yRoom - 1; j <= yRoom + 1; j++) {
                    if (i >= WORLDWIDTH || i < 0 || j >= WORLDHEIGHT
                            || j < 0 || world[i][j] == Tileset.WALL) {
                        continue;
                    }
                    if (world[i][j] == Tileset.LIGHTTHREE) {
                        world[i][j] = Tileset.LIGHTTWO;
                    }
                }
            }
        }
        lightOn = true;
    }

    public Area getLeftChild() {
        return leftChild;
    }

    public Area getRightChild() {
        return rightChild;
    }

    public void toggleLight(TETile[][] world) {
        for (int i = 0; i < WORLDWIDTH; i++) {
            for (int j = 0; j < WORLDHEIGHT; j++) {
                if (world[i][j] == Tileset.LIGHT || world[i][j] == Tileset.LIGHTTWO
                        || world[i][j] == Tileset.LIGHTTHREE) {
                    world[i][j] = Tileset.FLOOR;
                }
            }
        }
        lightOn = false;
    }

    public boolean getLightOn() {
        return lightOn;
    }

    public class Room {
        private int roomX;
        private int roomY;
        private int roomWidth;
        private int roomHeight;

        private boolean hasLight;
        public Room(int roomWidth, int roomHeight, int roomX, int roomY) {
            this.roomWidth = roomWidth;
            this.roomHeight = roomHeight;
            this.roomX = roomX;
            this.roomY = roomY;
            this.hasLight = false;
        }

        public boolean getHasLight() {
            return hasLight;
        }

        public int getRoomX() {
            return roomX;
        }

        public int getRoomY() {
            return roomY;
        }
    }

}

