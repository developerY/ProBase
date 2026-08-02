# Backend Deployment Architecture

## Hosting Strategy

KoColor follows a lightweight cloud architecture that separates backend services from static content delivery.

This approach allows each platform to focus on what it does best:

- **Rust Server** → Business logic, AI orchestration, APIs
- **GitHub** → Static image hosting and asset distribution

---

# Overall Architecture

```text
                Android / iOS / XR
                        │
                        ▼
                REST / HTTPS API
                        │
                        ▼
          Rust Backend (Hosted on Hugging Face)
                        │
      ┌─────────────────┼─────────────────┐
      ▼                 ▼                 ▼
 AI Processing     Business Logic      Data APIs
                        │
                        ▼
              Image URLs Returned
                        │
                        ▼
          GitHub Repository (Images)
                        │
                        ▼
          CDN / Raw Image Delivery
                        │
                        ▼
                  Mobile Client
```

---

# Rust Backend

## Hosted on Hugging Face

The Rust backend serves as the central intelligence layer of the application.

### Responsibilities

- REST API endpoints
- AI orchestration
- Business logic
- Product search
- Recommendation engine
- Color analysis
- Cosmetic intelligence
- Response generation

Because the backend is written in Rust, it provides:

- High performance
- Low memory usage
- Excellent concurrency
- Strong type safety
- Efficient asynchronous networking

---

# GitHub Image Repository

## Static Asset Hosting

Rather than storing images inside the backend server, product images are hosted separately in a GitHub repository.

Typical assets include:

- Cosmetic images
- Clothing photos
- Material swatches
- Color palettes
- Texture samples
- Demo assets

The backend simply returns image URLs.

```text
Rust API

↓

{
    "name": "Midnight Navy",
    "image":
    "https://.../midnight_navy.png"
}
```

The mobile application downloads the image directly from GitHub.

---

# Why Separate Images?

Keeping images outside the backend offers several architectural advantages.

## Smaller API Server

The Rust service focuses exclusively on computation rather than serving large binary files.

---

## Reduced Memory Usage

Large images are never loaded into the backend process unless required.

---

## Faster Deployment

Updating application assets does not require rebuilding or redeploying the Rust server.

Simply commit new images to GitHub.

---

## Version Control

Every image is automatically versioned.

Benefits include:

- Change history
- Easy rollbacks
- Pull request reviews
- Asset tracking

---

## CDN Distribution

GitHub provides efficient global delivery for static assets, reducing bandwidth requirements on the backend server.

---

# Request Flow

```text
User Opens Product
        │
        ▼
Android App
        │
        ▼
Rust API Request
        │
        ▼
Rust Server
(Hugging Face)
        │
        ▼
Returns JSON
        │
        ▼
Image URL
        │
        ▼
GitHub
        │
        ▼
Image Download
        │
        ▼
Displayed on Device
```

---

# Separation of Responsibilities

| Component | Responsibility |
|-----------|----------------|
| **Rust Backend** | APIs, business logic, AI, recommendations |
| **GitHub Repository** | Static images and media assets |
| **Mobile App** | UI rendering, caching, user interaction |

This separation keeps each layer focused on its primary responsibility.

---

# Benefits

## Scalability

The backend can scale independently of image hosting.

---

## Performance

API responses remain lightweight because they contain metadata and URLs rather than image binaries.

---

## Maintainability

Developers can update images without modifying backend code.

---

## Cost Efficiency

Separating compute from static content can reduce hosting costs and simplify infrastructure.

---

# Future Evolution

As KoColor grows, this architecture can evolve naturally.

```text
                Mobile Clients
                       │
                       ▼
             Rust API (Hugging Face)
                       │
      ┌────────────────┼────────────────┐
      ▼                ▼                ▼
 Recommendation     AI Engine      Color Science
                       │
                       ▼
                Metadata & URLs
                       │
                       ▼
             GitHub Image Repository
                       │
                       ▼
               Static Asset Delivery
```

Future enhancements could include dedicated object storage or a CDN for large-scale production deployments while preserving the same architectural separation between computation and static assets.

---

# Conclusion

Hosting the Rust backend on **Hugging Face** while storing static images in a **GitHub repository** creates a clean separation of concerns.

The backend focuses on delivering intelligent, high-performance APIs, while GitHub provides efficient hosting and versioning for static assets. This architecture is simple, maintainable, and well-suited for a modern, cloud-native application where computation and content delivery evolve independently.