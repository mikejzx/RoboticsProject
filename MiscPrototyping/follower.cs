using System;

/*
    C# version of a line-following concept,
    will be translated to C++ to better
    emulate the Arduino's lang and also make
    compiling on Linux much easier.
*/

namespace PrototypingArdCS.Follower {
    public class Follower {
        public static void Main (String[] args) {
            new Follower().Invoke(args);
        }

        private Vector2 curpos = Vector2.zero; // The current position of the robot.
        private Vector2[] nodes; // The nodes that would be sent from the application.
        private int curnode = 0;

        ///<summary>Main method (non-static)</summary>
        private void Invoke (String[] args) {
            Console.WriteLine("Hello, world");

            Initialise();

            while (true) {
                Loop();
                System.Threading.Thread.Sleep(10); // Sleep 10ms
            }
        }

        ///<summary>Initialise the main shit</summary>
        private void Initialise() {
            // These would be sent from the Android app in serialised
            // form and deserialised...
            // They are initialised here for simplicity's sake.
            nodes = new Vector2[] {
                new Vector2(200, 500),
                new Vector2(200, 700),
                new Vector2(700, 700),
                new Vector2(1000, 200)
            };
            
            curpos = nodes[0];
        }

        ///<summary>Called constantly</summary>
        private void Loop () {
            // The current pos should slowly lerp to the next
            // node, adjusting both left & right speeds accordingly
            // based on the angle between the nodes.

            // Print the output to the console
            string output = String.Format("\rPos={0} Idx={1} Node={2}", curpos.ToString(), curnode, nodes[curnode].ToString());
            Console.Write(output);
        }
    }

    struct Vector2 {
        int x, y;

        public static Vector2 zero { get { return new Vector2(0, 0); } }

        ///<summary>Ctor</summary>
        public Vector2 (int x, int y) {
            this.x = x;
            this.y = y;
        }

        public override string ToString() {
            return String.Format("[{0}, {1}]", x, y);
        }
    }
}
