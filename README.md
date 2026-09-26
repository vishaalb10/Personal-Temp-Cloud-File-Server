FileNest

FileNest is a simple way to share files temporarily. Upload a file, receive a five-digit passkey, and use that passkey to access the file from any device. No account is required.

## Web Site LInk

 [filenest.eu.cc](https://filenest.eu.cc).

## Features

- Upload files by dragging and dropping them into the page
- Receive an automatically generated five-digit passkey or choose your own
- View, open, and delete files using a passkey
- Keep recently used passkeys on the current device
- Switch between light and dark themes

## Stack

- Java 25 and Spring Boot 4
- Cloudinary for file storage
- A single HTML page styled with Tailwind CSS

## Run locally

Set your Cloudinary credentials and start the application:

```bash
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret
./mvnw spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

The included Dockerfile listens on `PORT`, which defaults to `8080`.

## API

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/upload` | Upload a `file`; optionally provide a `passkey`. |
| `GET` | `/files/{passkey}` | List files associated with a passkey. |
| `DELETE` | `/files/{passkey}/{publicId}` | Delete a file. |

## Notes

- Uploads are limited to 50 MB. Cloudinary's free plan limits most non-video files to 10 MB.
- Anyone with a passkey can access its files, so use a passkey that is difficult to guess.
- Files are not deleted automatically. Delete shared files within seven days.

