package Game;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class MultiGame {

    
   // teste
   private static final int MAX_PAUSA = 1000;
   private static final int MAX_COL = 10;
   private static final int MAX_LIN = 10;
   private static final char SIM_LIVRE = '.';
   private static final char SIM_BOLA = '@';
   private static final int NUN_PLAYERS = 4;
   private static final int NUM_TIMES = 2;
   private static final int LINE = 0;
   private static final int COLUMN = 1;
   private static final char TIME_1 = 'a';
   private static final char TIME_2 = 'A';

   private static final int PORTA = 1342; //1342
   private static byte[] buffer = new byte[1024];
   private static DatagramSocket socket;
   private static DatagramPacket packet;

   private static boolean venceu = false;

   private static char campo[][] = new char[MAX_LIN][MAX_COL];

   private static char players[] = new char[30];

   private static void inicializaJogo() {
       for (int i = 0; i < MAX_LIN; i++) {
           for (int j = 0; j < MAX_COL; j++) {
               campo[i][j] = SIM_LIVRE;
               if (j == 0) {
                   //campo[i][j] = Character.forDigit(i, MAX_LIN);
               }
           }
       }

       char[] vetPos = {'A', 'a'};
       for (int p = 0; p < NUM_TIMES; p++) {
           for (int i = 0; i < NUN_PLAYERS; i++) {  // time 1
               players[i] = (char) (vetPos[p] + i);
               paint(players[i]);
           }
       }
       paint(SIM_BOLA);

   }

   private static void paint(char objeto) {
       int lin = -1;
       int col = -1;
       do {
           lin = (int) (Math.random() * MAX_LIN);
           col = (int) (Math.random() * MAX_COL);

       } while (campo[lin][col] != SIM_LIVRE);
       campo[lin][col] = objeto;
   }

   private static void limpaTela() {
       for (int i = 0; i < 5; i++) {
           System.out.println("");
       }
   }

   private static void mostraCampo() {
       limpaTela();

       System.out.println();
       for (int i = 0; i < MAX_COL; i++) {
           System.out.print(TIME_1);
       }
       for (int i = 0; i < MAX_LIN; i++) {
           System.out.println();
           for (int j = 0; j < MAX_COL; j++) {
               System.out.print(campo[i][j]);
           }
       }
       System.out.println("");
       for (int i = 0; i < MAX_COL; i++) {
           System.out.print(TIME_2);
       }
       System.out.println("");


       try {
           Thread.sleep(MAX_PAUSA);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   public static void movePlayer(char player, char movimento) {
   /*   player, simbolo valido dos jogadores no campo
    movimentos:
            w ou W --> para cima
            x ou X --> para baixo
            a ou A --> para esquerda
            d ou D --> para direita
            sempre que for para cima da bola ela vai para o mesmo lado do
            movimento, no caso da latera, atravessa e começa no outro lado
            sempre que chegar a um extremo, time inverso ganha.

    aqui será recebida um pacote UDP com a movimentação de determinado jogador
   */

       int linhaPlayer = getColOrLinePlayerOrBall(player, LINE);
       int colunaPlayer = getColOrLinePlayerOrBall(player, COLUMN);
       int linhaBola = getColOrLinePlayerOrBall(SIM_BOLA, LINE);
       int colunaBola = getColOrLinePlayerOrBall(SIM_BOLA, COLUMN);

       switch (getMoveToLowerCase(movimento)) {
           case 'w':
               if (linhaBola != MAX_LIN)
                   executeBallMove(linhaBola, colunaBola, linhaBola - 1, colunaBola, linhaPlayer - 1, colunaPlayer);

               if (linhaPlayer > 0)
                   executePlayerMove(linhaPlayer, colunaPlayer, linhaPlayer - 1, colunaPlayer);
               break;
           case 'a': {

               if ((colunaBola) == 0)
                   campo[linhaBola][MAX_COL - 1] = campo[linhaBola][colunaBola];
               else
                   executeBallMove(linhaBola, colunaBola, linhaBola, colunaBola - 1, linhaPlayer, colunaPlayer - 1);

               if (colunaPlayer == 0)
                   executePlayerMove(linhaPlayer, colunaPlayer, linhaPlayer, MAX_COL - 1);
               else
                   executePlayerMove(linhaPlayer, colunaPlayer, linhaPlayer, colunaPlayer - 1);
               break;
           }
           case 's':
               //if ((linhaBola + 1) != MAX_LIN)
               executeBallMove(linhaBola, colunaBola, linhaBola + 1, colunaBola, linhaPlayer + 1, colunaPlayer);

               if (linhaPlayer != MAX_LIN) {
                   executePlayerMove(linhaPlayer, colunaPlayer, linhaPlayer + 1, colunaPlayer);
               }
               break;
           case 'd': {

               if ((colunaBola + 1) == MAX_COL)
                   executeBallMove(linhaBola, colunaBola, linhaBola, 0, linhaPlayer, colunaPlayer + 1);
               else
                   executeBallMove(linhaBola, colunaBola, linhaBola, colunaBola + 1, linhaPlayer, colunaPlayer + 1);

               if (colunaPlayer + 1 == MAX_COL) {
                   executeBallMove(linhaBola, colunaBola, linhaBola, colunaBola + 1, linhaPlayer, colunaPlayer + 1);
                   executePlayerMove(linhaPlayer, colunaPlayer, linhaPlayer, 0);

               } else
                   executePlayerMove(linhaPlayer, colunaPlayer, linhaPlayer, colunaPlayer + 1);

               break;
           }
       }

   }

   private static void executePlayerMove(int linhaPlayerOrig, int colunaPlayerOrig,
                                         int linhaPlayerDest, int colunaPlayerDest) {

       if (campo[linhaPlayerDest][colunaPlayerDest] == SIM_LIVRE /*|| campo[linhaPlayerDest][colunaPlayerDest] == SIM_BOLA*/) {
           campo[linhaPlayerDest][colunaPlayerDest] = campo[linhaPlayerOrig][colunaPlayerOrig];
           campo[linhaPlayerOrig][colunaPlayerOrig] = SIM_LIVRE;
       }
   }

   private static void executeBallMove(int linhaBolaOrig, int colunaBolaOrig, int linhaBolaDest, int colunaBolaDest,
                                       int linhaPlayer, int colunaPlayer) {
       if (isSamePosition(linhaBolaOrig, colunaBolaOrig, linhaPlayer, colunaPlayer)) {

           if (linhaBolaDest == MAX_LIN) {
               System.out.println("TIME " + TIME_1 + " VENCEU!!!");
               venceu = true;
           } else if (linhaBolaDest == -1) {
               System.out.println("TIME " + TIME_2 + " VENCEU!!!");
               venceu = true;
           } else {
               if (campo[linhaBolaDest][colunaBolaDest] == SIM_LIVRE) {
                   campo[linhaBolaDest][colunaBolaDest] = campo[linhaBolaOrig][colunaBolaOrig];
                   campo[linhaBolaOrig][colunaBolaOrig] = SIM_LIVRE;
               }
           }

       }
   }

   private static boolean isSamePosition(int linhaBolaOrig, int colunaBolaOrig, int linhaPlayer, int colunaPlayer) {
       return linhaPlayer == linhaBolaOrig && colunaPlayer == colunaBolaOrig;
   }

   private static char getMoveToLowerCase(char movimento) {
       return String.valueOf(movimento).toLowerCase().charAt(0);
   }

   public static boolean aguardaJogada() {
       try {
           socket.receive(packet);

           String efetiva = new String(buffer, 0, packet.getLength());
           String movimento = "";

           if (efetiva.startsWith("movePlayer(") && efetiva.endsWith(");")) {
               char player = getPlayerOrMoveFromMessage(efetiva, 0);
               char move = getPlayerOrMoveFromMessage(efetiva, 1);
               movePlayer(player, move);
               return true;
           }

       } catch (Exception e) {
           e.printStackTrace();
       }
       return false;
   }

   private static char getMoveFromMessage(String message) {
       String movimento;
       movimento = message.split(",")[1];
       return movimento.charAt(0);
   }

   private static char getPlayerOrMoveFromMessage(String message, int pos) {
       String playerOrMove;
       playerOrMove = message.split(",")[pos];
       if (pos == 0) return playerOrMove.charAt(playerOrMove.length() - 1);
       return playerOrMove.charAt(0);
   }


   public static int getColOrLinePlayerOrBall(char playerOrBall, int lineOrColumn) {
       for (int i = 0; i < MAX_LIN; i++) {
           for (int j = 0; j < MAX_COL; j++) {
               if (campo[i][j] == playerOrBall) {
                   if (lineOrColumn == LINE) return i;
                   return j;
               }
           }
       }
       return -1;
   }

   public static void main(String[] args) throws SocketException {
       inicializaJogo();

       socket = new DatagramSocket(PORTA);
       packet = new DatagramPacket(buffer, buffer.length);

       while (!venceu) {
           System.out.println("Aguardando nova jogada");
           mostraCampo();
           if (aguardaJogada()) {
               System.out.println("Jogada executada ");
           }
       }

   }
}

