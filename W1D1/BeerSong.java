public class BeerSong {
	public static void main (String[] args) {
		for (int beerNum = 99; beerNum > 0; beerNum--) {
			
			String word = getWord(beerNum);
		
			System.out.println(beerNum + " " + word + " of beer on the wall," );
			System.out.println(beerNum + " " + word + " of beer!" ) ;
			System.out.println("Take one down," );
			System.out.println("Pass it around," );
			
			if (beerNum > 1) {
				System.out.println(beerNum - 1 + " " + getWord(beerNum - 1) + " of beer on the wall!" );
			} else {
				System.out.println("No more bottles of beer on the wall!");
			}
			
			System.out.println("");
		}
	}
	
	public static String getWord(int num) {
		//returns bottle or bottles based on numbed passed to function
		if (num > 1) {
			return "bottles";
		} else {
			return "bottle";
		}
	}
}