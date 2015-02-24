public class PhraseOMatic {
	public static void main (String[] args) {

		String[] wordOne = {"24/7", "multi-tier", "30,000 foot", "B-to-B",
		"win-win", "front-end", "web-based", "pervasive", "smart"};
		String[] wordTwo = {"empowered", "sticky", "value-added", "oriented",
		"centric", "distributed", "clustered", "branded", "outside-the-box"};
		String[] wordThree = {"process", "tipping-point", "strategy", "mindshare",
		"portal", "space", "vision", "paradigm", "mission"};

		int rand1 = (int) (Math.random() * wordOne.length);
		int rand2 = (int) (Math.random() * wordTwo.length);
		int rand3 = (int) (Math.random() * wordThree.length);

		String phrase = wordOne[rand1] + " " + wordTwo[rand2] + " " + wordThree[rand3];

		System.out.println("What we need is a " + phrase);
	}
}