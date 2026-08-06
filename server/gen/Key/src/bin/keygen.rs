use ed25519_dalek::SigningKey;
use rand::rngs::OsRng;

fn main() {
    let mut csprng = OsRng;
    let signing_key = SigningKey::generate(&mut csprng);
    let public_key = signing_key.verifying_key();

    println!("🔒 PRIVATE KEY (Keep Secret!):");
    println!("{}", hex::encode(signing_key.to_bytes()));

    println!("\n🔑 PUBLIC KEY (Ship in Android App):");
    println!("{}", hex::encode(public_key.as_bytes()));
}