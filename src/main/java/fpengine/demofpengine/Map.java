package fpengine.demofpengine;

import javafx.util.Pair;

import java.io.*;
import java.util.Random;
import java.util.Stack;

public class Map {
    private int Height;
    private int Width;
    private char[][] map;
    public Map(int w, int h)
    {
            Height = h;
            Width = w;
            map = new char[Height][Width];

            for(int i = 0; i < Height;i++)
                for(int j =0;j<Width;j++) {
                    map[i][j] = '#';
                    if(i > 0 && i < Height - 1 && j > 0 && j < Width - 1)
                        map[i][j] = '.';
                }
            map[5][5] = '#';
            map[6][5] = '#';
            map[7][5] = '#';
    }


    public Map(int w, int h, boolean generate, int playerX, int playerY)
    {
        Height = h;
        Width = w;
        DFS(playerX,playerY);
    }
    public Map(String Filepath) throws IOException {
        RandomAccessFile fIn = new RandomAccessFile(Filepath,"r");
        int ch;
        Height = 0;
        Width = 0;
        int i = 0;
        int j = 0;

        while ((ch = fIn.read()) != -1 )
        {
            if((char)ch == '|')
                Height++;
            if((char)ch == '-')
                Width++;
        }
        Height -= 1;
        map = new char[Height][Width];
        fIn.seek(0);

        while ((ch = fIn.read()) != - 1)
        {
            if((char)ch == 'X')
            {
                map[i][j] = '#';
                j++;
            }
            if((char)ch == 'H')
            {
                map[i][j] = 'H';
                j++;
            }
            if((char)ch == '.'  || (char)ch == 'O' || (char)ch == 'v' || (char)ch == 'S' || (char)ch == 'c' || (char)ch == '^')
            {
                map[i][j] = '.';
                j++;
            }
            if((char)ch == 'd')
            {
                map[i][j] = 'd';
                j++;
            }
            if((char)ch == '|')
            {
                j = 0;
                i++;
            }
            if((char)ch == '-')
                break;
        }
        fIn.close();
        print();
    }

    public int getHeight()
    {
        return Height;
    }
    public int getWidth()
    {
        return Width;
    }
    public char getMap(int X, int Y) {
        return map[Y][X];
    }
    public void setMap(int X, int Y, char value){ map[Y][X] = value; }
    public void print()
    {
        for(int i = 0; i < Height;i++)
        {
            for(int j =0;j<Width;j++)
            {
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println(Width + "x" + Height);
    }
    public String export()
    {
        StringBuilder MiniMap = new StringBuilder();
        for(int i = 0;i < Height; i++)
        {
            for(int j = 0;j<Width; j++)
            {
                MiniMap.append(map[i][j]);
                MiniMap.append(" ");
            }
            MiniMap.append("\n");
        }
        return MiniMap.toString();
    }
    private boolean checker(boolean[][] grid, int x, int y)
    {
        if(x + 1 < Width)
        {
            if (!grid[y][x + 1])
            {
                return true;
            }
        }
        if(x - 1 >= 0)
        {
            if (!grid[y][x - 1])
            {
                return true;
            }
        }
        if(y - 1 >= 0)
        {
            if (!grid[y - 1][x])
            {
                return true;
            }
        }
        if(y + 1 < Height)
        {
            return !grid[y + 1][x];
        }
        return false;
    }
    /**Metoda Iteracyjna DFS sluzaca do wygenerowania Labiryntu*/
    public void DFS(int k, int l) {
        boolean[][] grid = new boolean[Height][Width];

        for(int i = 0; i < Height; i++)
        {
            for (int j = 0; j < Width; j++)
            {
                grid[i][j] = false;
            }
        }

        grid[l][k] = true; // [y][x]

        //3+ (x-2)*2 - wyliczone bruteforcem
        //+2 dodajemy na koncu bo obramowanie
        int mapH = 3 + ((Height - 2) * 2) + 2;
        int mapW = 3 + ((Width - 2) * 2) + 2;
        map = new char[mapH][mapW];

        for(int i = 0;i < mapH; i++) {
            for (int j = 0; j< mapW;j++)
            {
                map[i][j] = '#';
                if(i%2==1 && j%2==1)//bierzemy tylko jak pary sa nieparzyste
                    if( (i!=mapH - 1 && j!=mapW-1) || (i!=0 && j!=0) )//zostawiamy obramowania
                        map[i][j] = '.';
            }
        }



        Stack<Pair<Integer, Integer>> stack = new Stack<>();
        stack.push(new Pair<>(k,l));
        while (!stack.empty())
        {
            Pair<Integer, Integer> para = stack.pop();
            int x = para.getKey();
            int y = para.getValue();
            if(checker(grid, x, y))
            {
                stack.push(new Pair<>(x,y));

                Random random = new Random();
                int chance = random.nextInt(4);

                if(chance == 0)//prawo
                {
                    if(x + 1 < Width)
                    {
                        if(!grid[y][x + 1])
                        {

                            map[2*y + 1][2*x+1 +1] = '.';//2*x+1 - koordynaty punktow
                            //+1 czyli sciana po prawo
                            x += 1;
                            stack.push(new Pair<>(x,y));
                            grid[y][x] = true;
                        }
                    }
                }
                else if(chance == 1)//lewo
                {
                    if(x - 1 >= 0)
                    {
                        if(!grid[y][x - 1])
                        {

                            map[2*y + 1][2*x+1 -1] = '.';//2*x+1 - koordynaty punktow
                            //-1 czyli sciana po lewo
                            x -= 1;
                            stack.push(new Pair<>(x,y));
                            grid[y][x] = true;
                        }
                    }
                }
                else if(chance == 2)//gora
                {
                    if(y - 1 >= 0)
                    {
                        if(!grid[y - 1][x])
                        {

                            map[2*y+1 -1][2*x+1] = '.';//2*x+1 - koordynaty punktow
                            //-1 czyli sciana na gore
                            y -= 1;
                            stack.push(new Pair<>(x,y));
                            grid[y][x] = true;
                        }
                    }
                }
                else//dol
                {
                    if(y + 1 < Height)
                    {
                        if(!grid[y + 1][x])
                        {

                            map[2*y+1 +1][2*x+1] = '.';//2*x+1 - koordynaty punktow
                            //+1 czylisciana na dole
                            y += 1;
                            stack.push(new Pair<>(x,y));
                            grid[y][x] = true;
                        }
                    }
                }
            }
        }
        Height = mapH;
        Width = mapW;
        print();
        System.out.println("H: " + mapH + " W: " + mapW);
    }

}
